package com.employee.services.serviceImpl;

import com.employee.domain.entity.Employee;
import com.employee.domain.entity.EngagementResponse;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.dtos.request.EngagementRequestDto;
import com.employee.dtos.response.EngagementResponseDto;
import com.employee.dtos.response.EngagementResultDto;
import com.employee.exceptions.DuplicateResourceException;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.EmployeeRepository;
import com.employee.repositories.EngagementResponseRepository;
import com.employee.services.service.EngagementService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EngagementServiceImpl implements EngagementService {

    private static final ZoneId OFFICE_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MIN_GROUP_SIZE = 3;     // privacy: no averages for fewer than 3 people

    private final EngagementResponseRepository responseRepository;
    private final EmployeeRepository employeeRepository;

    public EngagementServiceImpl(EngagementResponseRepository responseRepository,
                                 EmployeeRepository employeeRepository) {
        this.responseRepository = responseRepository;
        this.employeeRepository = employeeRepository;
    }

    // An employee answers this month's pulse check (once)
    @Override
    @Transactional
    public EngagementResponseDto submit(EngagementRequestDto request, Authentication authentication) {
        Employee employee = findCaller(authentication);
        if (employee.getStatus() != EmployeeStatus.ACTIVE && employee.getStatus() != EmployeeStatus.NOTICE_PERIOD) {
            throw new IllegalStateException("Only working employees can answer the pulse check");
        }

        String month = YearMonth.now(OFFICE_ZONE).toString();               // "2026-10"
        if (responseRepository.existsByEmployeeIdAndMonth(employee.getId(), month)) {
            throw new DuplicateResourceException("You already answered the pulse check for " + month);
        }

        EngagementResponse response = new EngagementResponse();
        response.setEmployee(employee);
        response.setMonth(month);
        response.setJobSatisfaction(request.getJobSatisfaction());
        response.setManagerSupport(request.getManagerSupport());
        response.setWorkLifeBalance(request.getWorkLifeBalance());
        response.setCareerGrowth(request.getCareerGrowth());
        response.setComment(request.getComment() == null ? null : request.getComment().trim());
        responseRepository.save(response);

        return summarise("My response", List.of(response), false);          // the employee sees their own answers
    }

    // An employee views their own answers for a month
    @Override
    @Transactional(readOnly = true)
    public EngagementResponseDto myResponse(YearMonth month, Authentication authentication) {
        Employee employee = findCaller(authentication);
        EngagementResponse response = responseRepository.findByEmployeeIdAndMonth(employee.getId(), month.toString())
                .orElseThrow(() -> new ResourceNotFoundException("You haven't answered the pulse check for " + month));
        return summarise("My response", List.of(response), false);
    }

    @Override
    @Transactional(readOnly = true)
    public EngagementResultDto results(YearMonth month, Authentication authentication) {

        // 1. Who is asking? Only HR, Admin or a manager may see results (checked here too, not only in the controller)
        boolean isHrOrAdmin = isHrOrAdmin(authentication);
        boolean isManager = hasRole(authentication, "ROLE_MANAGER");

        if (!isHrOrAdmin && !isManager) {
            throw new AccessDeniedException("You are not allowed to view engagement results");
        }

        // 2. All responses for the month
        List<EngagementResponse> all = responseRepository.findByMonth(month.toString());

        // 3. Group them by department name
        Map<String, List<EngagementResponse>> byDepartment = all.stream()
                .filter(r -> r.getEmployee().getDepartment() != null)
                .collect(Collectors.groupingBy(r -> r.getEmployee().getDepartment().getName()));

        // 4. A manager (who isn't also HR/Admin) only sees their own department
        if (!isHrOrAdmin) {
            Employee manager = findCaller(authentication);
            String ownDepartment = manager.getDepartment() == null ? null : manager.getDepartment().getName();
            byDepartment.keySet().removeIf(name -> !name.equals(ownDepartment));
        }

        // 5. Summarise each department (hidden when fewer than 3 people answered)
        List<EngagementResponseDto> departments = byDepartment.entrySet().stream()
                .map(e -> summarise(e.getKey(), e.getValue(), true))
                .sorted(Comparator.comparing(EngagementResponseDto::name))
                .toList();

        // 6. The company-wide line is only for HR/Admin
        EngagementResponseDto company = isHrOrAdmin ? summarise("Company", all, true) : null;

        return new EngagementResultDto(month.toString(), company, departments);
    }

    // ------------------------------------------------------------------ helpers

    // Averages for a group. With applyPrivacy, groups smaller than 3 are hidden.
    private EngagementResponseDto summarise(String name, List<EngagementResponse> responses, boolean applyPrivacy) {
        if (responses.isEmpty() || (applyPrivacy && responses.size() < MIN_GROUP_SIZE)) {
            return new EngagementResponseDto(name, responses.size(), null, null, null, null, null, !responses.isEmpty());
        }
        Double job = average(responses, EngagementResponse::getJobSatisfaction);
        Double manager = average(responses, EngagementResponse::getManagerSupport);
        Double balance = average(responses, EngagementResponse::getWorkLifeBalance);
        Double growth = average(responses, EngagementResponse::getCareerGrowth);
        Double percent = round1((job + manager + balance + growth) / 4 / 5 * 100);   // 4.2 average → 84%
        return new EngagementResponseDto(name, responses.size(), job, manager, balance, growth, percent, false);
    }

    private static Double average(List<EngagementResponse> responses, Function<EngagementResponse, Integer> field) {
        return round1(responses.stream().mapToInt(field::apply).average().orElse(0));
    }

    private static Double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private Employee findCaller(Authentication authentication) {
        return employeeRepository.findByEmail(authentication.getName().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile found for " + authentication.getName()));
    }

    // True if the logged-in user has the given role, e.g. "ROLE_MANAGER"
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private boolean isHrOrAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_HR") || hasRole(authentication, "ROLE_ADMIN");
    }
}
