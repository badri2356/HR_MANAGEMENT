package com.employee.services.serviceImpl;

import com.employee.domain.entity.Employee;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.LeaveStatus;
import com.employee.domain.enums.RiskLevel;
import com.employee.dtos.response.*;
import com.employee.repositories.EmployeeRepository;
import com.employee.repositories.LeaveRequestRepository;
import com.employee.repositories.PayrollRunRepository;
import com.employee.services.service.AdminDashboardService;
import com.employee.services.service.AttendanceService;
import com.employee.services.service.EngagementService;
import com.employee.services.service.InsightService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId OFFICE_ZONE = ZoneId.of("Asia/Kolkata");
    private static final List<EmployeeStatus> WORKING = List.of(EmployeeStatus.ACTIVE, EmployeeStatus.NOTICE_PERIOD);

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final AttendanceService attendanceService;
    private final InsightService insightService;
    private final EngagementService engagementService;

    public AdminDashboardServiceImpl(EmployeeRepository employeeRepository, LeaveRequestRepository leaveRequestRepository, PayrollRunRepository payrollRunRepository, AttendanceService attendanceService, InsightService insightService, EngagementService engagementService) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.attendanceService = attendanceService;
        this.insightService = insightService;
        this.engagementService = engagementService;
    }


    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard(Authentication authentication) {

        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() != EmployeeStatus.EXITED)
                .toList();
        Map<String, Long> byStatus = employees.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus().name(), TreeMap::new, Collectors.counting()));

        TreeMap<String, Long> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(e -> e.getDepartment() == null ? "No department" : e.getDepartment().getName(), TreeMap::new, Collectors.counting()));

        AdminDashboardDto.Headcount headcount = new AdminDashboardDto.Headcount(employees.size(), byStatus, byDepartment);

        AttendanceSummaryDto todaySummary = attendanceService.attendanceSummary();

        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);

        AdminDashboardDto.LatestPayroll latestPayroll = payrollRunRepository.findFirstByOrderByYearDescMonthDesc()
                .map(
                        run -> new AdminDashboardDto.LatestPayroll(
                                YearMonth.of(run.getYear(), run.getMonth()).toString(),
                                run.getStatus().name(),
                                run.getTotalNet(),
                                run.getEmployeeCount()
                        )
                )
                .orElse(null);
        List<AttritionInsightDto> attrition = insightService.getAttritionRisk(authentication);
        AdminDashboardDto.AttritionSummary attritionSummary = new AdminDashboardDto.AttritionSummary(
                attrition.stream().filter(a -> a.riskLevel() == RiskLevel.HIGH).count(),
                attrition.stream().filter(a -> a.riskLevel() == RiskLevel.MEDIUM).count(),
                attrition.stream().filter(a -> a.riskLevel() == RiskLevel.LOW).count(),
                attrition.stream().limit(3).toList()
        );

        List<SkillGapDto> gaps = insightService.getSkillGaps().stream()
                .filter(e -> !e.getMissingSkills().isEmpty())
                .toList();

        EngagementResultDto engagement = engagementService.results(YearMonth.now(OFFICE_ZONE), authentication);
        Double engagementPercent = engagement.company() == null ? null : engagement.company().engagementPercent();

        return new AdminDashboardDto(headcount, todaySummary, pendingLeaves, latestPayroll,
                attritionSummary, gaps, engagementPercent);
    }
}
