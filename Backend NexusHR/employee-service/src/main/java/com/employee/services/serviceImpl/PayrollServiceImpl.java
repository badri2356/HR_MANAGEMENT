package com.employee.services.serviceImpl;

import com.employee.domain.entity.Employee;
import com.employee.domain.entity.PayrollRun;
import com.employee.domain.entity.Payslip;
import com.employee.domain.entity.SalaryStructure;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.NotificationType;
import com.employee.domain.enums.PayrollStatus;
import com.employee.dtos.request.SalaryStructureRequestDto;
import com.employee.dtos.response.PayrollRunResponseDto;
import com.employee.dtos.response.PayslipResponseDto;
import com.employee.dtos.response.SalaryStructureResponseDto;
import com.employee.events.NotificationEvent;
import com.employee.exceptions.DuplicateResourceException;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.*;
import com.employee.services.service.AttendanceService;
import com.employee.services.service.PayrollService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PayrollServiceImpl implements PayrollService {

    public static final BigDecimal PF_RATE = new BigDecimal("0.12");
    public static final BigDecimal PROFESSIONAL_TAX = new BigDecimal("200.00");
    public static final BigDecimal TDS_RATE = new BigDecimal("0.05");

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;
    private final PayrollRunRepository payrollRunRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PayrollServiceImpl(SalaryStructureRepository salaryStructureRepository,
                              PayslipRepository payslipRepository,
                              EmployeeRepository employeeRepository,
                              AttendanceRepository attendanceRepository,
                              AttendanceService attendanceService,
                              PayrollRunRepository payrollRunRepository, ApplicationEventPublisher eventPublisher) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.payslipRepository = payslipRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceService = attendanceService;
        this.payrollRunRepository = payrollRunRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public SalaryStructureResponseDto setSalary(Long employeeId, SalaryStructureRequestDto request) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee does not exist with employee id " + employeeId)
                );
        if (employee.getStatus().equals(EmployeeStatus.EXITED)) {
            throw new IllegalStateException("Exited employee can't create salary structure");
        }

        SalaryStructure salary = salaryStructureRepository.findByEmployeeId(employeeId)
                .orElseGet(
                        () -> {
                            SalaryStructure created = new SalaryStructure();
                            created.setEmployee(employee);
                            return created;
                        }
                );
        salary.setBasic(money(request.getBasic()));
        salary.setHra(money(request.getHra()));
        salary.setAllowances(money(request.getAllowances()));
        salary.setPfApplicable(request.getPfApplicable() == null || request.getPfApplicable());

        return toSalaryResponse(salaryStructureRepository.save(salary));
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponseDto getSalary(Long employeeId, Authentication authentication) {

        SalaryStructure salary = salaryStructureRepository.findByEmployeeId(employeeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Salary not set for employee " + employeeId)
                );
        boolean isOwner = salary.getEmployee().getEmail().equalsIgnoreCase(authentication.getName());

        if (!isOwner && !isHrOrAdmin(authentication)) {
            throw new AccessDeniedException("You can only view own salary");
        }
        return toSalaryResponse(salary);
    }

    @Override
    @Transactional   // all-or-nothing: the run and all its payslips are saved together, or nothing is
    public PayrollRunResponseDto createRun(YearMonth month) {

        // 1. Payroll can't be run for a month that hasn't happened yet
        //    (no attendance exists for future days, so everyone would be paid in full)
        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Kolkata"));
        if (month.isAfter(currentMonth)) {
            throw new IllegalStateException("Cannot run payroll for " + month + ": that month hasn't started yet");
        }

        // 2. Only one run per month
        if (payrollRunRepository.existsByYearAndMonth(month.getYear(), month.getMonthValue())) {
            throw new DuplicateResourceException("Payroll for " + month + " already exists");
        }

        // 3. No attendance at all this month → it wasn't tracked; payroll would mark everyone absent
        if (!attendanceService.hasAttendanceData(month)) {
            throw new IllegalStateException("No attendance was recorded for " + month
                    + ". Payroll would treat everyone as absent, so it can't be run.");
        }

        // 3. Working days in the month (Mon–Fri), e.g. 22 for October 2026
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        int workingDays = countWorkingDays(startDate, endDate);

        // 4. Who should be paid: active employees, and those still serving their notice period
        List<Employee> employees = employeeRepository.findByStatusIn(
                List.of(EmployeeStatus.ACTIVE, EmployeeStatus.NOTICE_PERIOD));

        // 5. Load all salaries in ONE query, then look them up by employee id
        Map<Long, SalaryStructure> salaries = salaryStructureRepository
                .findByEmployeeIdIn(employees.stream().map(Employee::getId).toList())
                .stream()
                .collect(Collectors.toMap(s -> s.getEmployee().getId(), Function.identity()));

        // 6. Save the run first, so every payslip can point to its id
        PayrollRun run = new PayrollRun();
        run.setYear(month.getYear());
        run.setMonth(month.getMonthValue());
        run.setStatus(PayrollStatus.DRAFT);
        run.setWorkingDays(workingDays);
        run = payrollRunRepository.save(run);

        List<Payslip> payslips = new ArrayList<>();
        List<String> skipped = new ArrayList<>();   // tells HR who was left out, and why

        // 7. One payslip per eligible employee
        for (Employee employee : employees) {
            String name = employee.getFirstName() + " " + employee.getLastName();

            SalaryStructure salary = salaries.get(employee.getId());
            if (salary == null) {
                skipped.add(name + " (salary not set)");
                continue;                               // no salary → no payslip
            }

            int employedDays = countEmployeeWorkingDays(employee, startDate, endDate);
            if (employedDays == 0) {
                skipped.add(name + " (not employed this month)");
                continue;                               // joined after, or left before, this month
            }

            int absentDays = attendanceService.findAbsentDates(employee, month).size();
            payslips.add(calculatePayslip(run, employee, salary, workingDays, employedDays, absentDays));
        }

        // 8. Nobody could be paid → stop; @Transactional rolls back the empty run too
        if (payslips.isEmpty()) {
            throw new IllegalStateException("No payslips could be created. Skipped: " + skipped);
        }

        // 9. Save all payslips in one go
        payslipRepository.saveAll(payslips);

        // 10. Totals for the whole run
        run.setEmployeeCount(payslips.size());
        run.setTotalGross(sum(payslips, Payslip::getGrossPay));
        run.setTotalDeductions(sum(payslips, Payslip::getTotalDeductions));
        run.setTotalNet(sum(payslips, Payslip::getNetPay));

        return toRunResponse(payrollRunRepository.save(run), payslips, skipped);
    }

    private Payslip calculatePayslip(PayrollRun run, Employee employee, SalaryStructure salary,
                                     int workingDays, int employedDays, int absentDays) {

        int safeAbsentDays = Math.min(Math.max(absentDays, 0), employedDays);   // never negative, never more than employed days
        int payableDays = employedDays - safeAbsentDays;

        BigDecimal days = BigDecimal.valueOf(workingDays);
        BigDecimal employed = BigDecimal.valueOf(employedDays);
        BigDecimal absent = BigDecimal.valueOf(safeAbsentDays);
        BigDecimal payable = BigDecimal.valueOf(payableDays);

        // 1. Full monthly salary (from the salary structure)
        BigDecimal monthlySalary = salary.getBasic().add(salary.getHra()).add(salary.getAllowances());

        // 2. Salary for one working day (kept precise, NOT rounded yet)
        BigDecimal perDaySalary = monthlySalary.divide(days, 10, RoundingMode.HALF_UP);
        BigDecimal perDayBasic = salary.getBasic().divide(days, 10, RoundingMode.HALF_UP);

        // 3. Earnings for the days employed (prorated for mid-month joiners and leavers)
        BigDecimal grossEarnings = money(perDaySalary.multiply(employed));

        // 4. Loss of pay covers ONLY absent days
        BigDecimal lossOfPay = money(perDaySalary.multiply(absent));

        // 5. What is actually paid
        BigDecimal grossPay = grossEarnings.subtract(lossOfPay);
        BigDecimal earnedBasic = perDayBasic.multiply(payable);

        // 6. Deductions
        BigDecimal pf = salary.isPfApplicable()
                ? money(earnedBasic.multiply(PF_RATE))
                : BigDecimal.ZERO.setScale(2);
        BigDecimal professionalTax = grossPay.signum() > 0
                ? money(PROFESSIONAL_TAX)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal tds = money(grossPay.multiply(TDS_RATE));

        BigDecimal totalDeductions = pf.add(professionalTax).add(tds);
        BigDecimal netPay = grossPay.subtract(totalDeductions).max(BigDecimal.ZERO.setScale(2));

        // 7. Build the payslip
        Payslip payslip = new Payslip();
        payslip.setPayrollRun(run);
        payslip.setEmployee(employee);
        payslip.setWorkingDays(workingDays);
        payslip.setAbsentDays(safeAbsentDays);
        payslip.setBasic(salary.getBasic());               // original monthly components, kept as a snapshot
        payslip.setHra(salary.getHra());
        payslip.setAllowances(salary.getAllowances());
        payslip.setGrossEarnings(grossEarnings);
        payslip.setLossOfPay(lossOfPay);
        payslip.setGrossPay(grossPay);
        payslip.setPf(pf);
        payslip.setProfessionalTax(professionalTax);
        payslip.setTds(tds);
        payslip.setTotalDeductions(totalDeductions);
        payslip.setNetPay(netPay);
        return payslip;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollRunResponseDto> listRun() {

        return payrollRunRepository.findAllByOrderByYearDescMonthDesc()
                .stream()
                .map(run -> toRunResponse(run, List.of(), List.of()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollRunResponseDto getRun(Long runId) {

        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Payroll is not found with pay run id " + runId)
                );
        List<Payslip> payslips = payslipRepository.findByPayrollRunIdOrderByEmployeeFirstNameAsc(runId);

        return toRunResponse(run, payslips, List.of());
    }

    @Override
    @Transactional
    public PayrollRunResponseDto processRun(Long runId, String adminEmail) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Payroll is not found with pay run id " + runId)
                );
        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only a DRAFT run can be processed (current: " + run.getStatus() + ")");
        }
        run.setStatus(PayrollStatus.PROCESSED);
        run.setProcessedBy(adminEmail);
        return toRunResponse(payrollRunRepository.save(run), List.of(), List.of());
    }

    @Override
    @Transactional
    public PayrollRunResponseDto payRun(Long runId, String adminEmail) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Payroll is not found with pay run id " + runId)
                );
        if (run.getStatus() != PayrollStatus.PROCESSED) {
            throw new IllegalStateException("Only a PROCESSED run can be paid (current: " + run.getStatus() + ")");
        }
        run.setStatus(PayrollStatus.PAID);
        run.setPaidBy(adminEmail);

        PayrollRun save = payrollRunRepository.save(run);

        String month = YearMonth.of(save.getYear(), save.getMonth()).toString();
        for (Payslip payslip : payslipRepository.findByPayrollRunIdOrderByEmployeeFirstNameAsc(save.getId())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    payslip.getEmployee().getEmail(),
                    NotificationType.PAYROLL_PAID,
                    "Your payslip for " + month + " is ready. Net pay: ₹" + payslip.getNetPay() + "."));
        }

        return toRunResponse(save, List.of(), List.of());
    }

    @Override
    @Transactional
    public void deleteRun(Long runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Payroll is not found with pay run id " + runId)
                );
        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only a DRAFT run can be deleted");
        }
        payslipRepository.deleteByPayrollRunId(runId);
        payrollRunRepository.delete(run);

    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponseDto> myPayslips(String email) {
        Employee employee = employeeRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for " + email));

        return payslipRepository.findByEmployeeId(employee.getId()).stream()
                .filter(p -> p.getPayrollRun().getStatus() == PayrollStatus.PAID)          // only paid months
                .sorted(Comparator.comparing((Payslip p) -> p.getPayrollRun().getYear())
                        .thenComparing(p -> p.getPayrollRun().getMonth())
                        .reversed())                                                     // newest first
                .map(this::toPayslipResponse)
                .toList();
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_DOWN);
    }

    private SalaryStructureResponseDto toSalaryResponse(SalaryStructure salary) {
        SalaryStructureResponseDto response = new SalaryStructureResponseDto();
        response.setEmployeeId(salary.getEmployee().getId());
        response.setEmployeeName(salary.getEmployee().getFirstName() + " " + salary.getEmployee().getLastName());
        response.setBasic(salary.getBasic());
        response.setHra(salary.getHra());
        response.setAllowances(salary.getAllowances());
        response.setGrossMonthly(salary.getBasic().add(salary.getHra()).add(salary.getAllowances()));
        response.setPfApplicable(salary.isPfApplicable());
        return response;
    }

    private boolean isHrOrAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    private static int countWorkingDays(LocalDate from, LocalDate to) {

        return (int) from.datesUntil(to.plusDays(1))
                .filter(day ->
                        day.getDayOfWeek() != DayOfWeek.SATURDAY
                                && day.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    private static int countEmployeeWorkingDays(Employee employee, LocalDate monthStart, LocalDate monthEnd) {
        LocalDate from = employee.getJoiningDate().isAfter(monthStart)
                ? employee.getJoiningDate()
                : monthStart;
        LocalDate to = employee.getExitDate() != null && employee.getExitDate().isBefore(monthEnd)
                ? employee.getExitDate()
                : monthEnd;

        return to.isBefore(from) ? 0 : countWorkingDays(from, to);
    }

    // Adds up one money field across all payslips, e.g. the total net pay of the run
    private static BigDecimal sum(List<Payslip> payslips, Function<Payslip, BigDecimal> field) {
        return payslips.stream()
                .map(field)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PayrollRunResponseDto toRunResponse(PayrollRun run, List<Payslip> payslips, List<String> skipped) {
        PayrollRunResponseDto response = new PayrollRunResponseDto();
        response.setId(run.getId());
        response.setMonth(YearMonth.of(run.getYear(), run.getMonth()).toString());   // "2026-10"
        response.setStatus(run.getStatus());
        response.setWorkingDays(run.getWorkingDays());
        response.setEmployeeCount(run.getEmployeeCount());
        response.setTotalGross(run.getTotalGross());
        response.setTotalDeduction(run.getTotalDeductions());
        response.setTotalNet(run.getTotalNet());
        response.setProcessedBy(run.getProcessedBy());
        response.setPaidBy(run.getPaidBy());
        response.setPayslips(payslips.stream().map(this::toPayslipResponse).toList());
        response.setSkippedEmployees(skipped);
        return response;
    }

    private PayslipResponseDto toPayslipResponse(Payslip p) {
        PayslipResponseDto response = new PayslipResponseDto();
        response.setId(p.getId());
        response.setMonth(YearMonth.of(p.getPayrollRun().getYear(), p.getPayrollRun().getMonth()).toString());
        response.setEmployeeId(p.getEmployee().getId());
        response.setEmployeeName(p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName());
        response.setWorkingDays(p.getWorkingDays());
        response.setAbsentDays(p.getAbsentDays());
        response.setBasic(p.getBasic());
        response.setHra(p.getHra());
        response.setAllowance(p.getAllowances());
        response.setGrossEarnings(p.getGrossEarnings());
        response.setLossOfPay(p.getLossOfPay());
        response.setGrossPay(p.getGrossPay());
        response.setPf(p.getPf());
        response.setProfessionalTax(p.getProfessionalTax());
        response.setTds(p.getTds());
        response.setTotalDeduction(p.getTotalDeductions());
        response.setNetPay(p.getNetPay());
        return response;
    }
}
