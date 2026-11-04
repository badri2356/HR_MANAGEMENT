package com.employee.services.serviceImpl;

import com.employee.domain.entity.*;
import com.employee.domain.enums.*;
import com.employee.dtos.response.AttritionInsightDto;
import com.employee.dtos.response.SkillGapDto;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.insights.RequiredSkills;
import com.employee.repositories.*;
import com.employee.services.service.AttendanceService;
import com.employee.services.service.InsightService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightServiceImpl implements InsightService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final GoalRepository goalRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceService attendanceService;

    public InsightServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                              AttendanceRepository attendanceRepository, PerformanceReviewRepository reviewRepository, GoalRepository goalRepository,
                              LeaveRequestRepository leaveRequestRepository, AttendanceService attendanceService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.reviewRepository = reviewRepository;
        this.goalRepository = goalRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendanceService = attendanceService;
    }


    @Override
    @Transactional
    public List<SkillGapDto> getSkillGaps() {

        List<Employee> employees = employeeRepository.findWithSkillsByStatusIn(List.of(EmployeeStatus.ACTIVE, EmployeeStatus.NOTICE_PERIOD));

        Map<Long, List<Employee>> byDepartment = employees.stream()
                .filter(i -> i.getDepartment() != null)
                .collect(Collectors.groupingBy(i -> i.getDepartment().getId()));

        return departmentRepository.findAll().stream()
                .map(department -> buildSkillGap(
                        department,
                        byDepartment.getOrDefault(department.getId(), List.of())))
                .sorted(Comparator.comparingInt(
                        SkillGapDto::getPercentage))
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<AttritionInsightDto> getAttritionRisk(Authentication authentication) {
        List<Employee> employees = List.of();
        if (hasRole(authentication, "ROLE_HR") || hasRole(authentication, "ROLE_ADMIN")) {
            employees = employeeRepository.findWithSkillsByStatusIn(List.of(EmployeeStatus.ACTIVE, EmployeeStatus.NOTICE_PERIOD));
        } else if (hasRole(authentication, "ROLE_MANAGER")) {
            Employee manager = employeeRepository.findByEmail(authentication.getName().toLowerCase())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("No employee profile found for employee id " + authentication.getName())
                    );
            employees = employeeRepository.findByManagerIdAndStatusIn(
                    manager.getId(),
                    List.of(EmployeeStatus.ACTIVE, EmployeeStatus.NOTICE_PERIOD)
            );
        } else {
            throw new AccessDeniedException("You are not allowed to view attrition risk");
        }
        LocalDate firstWorkDate = attendanceRepository.findFirstByOrderByWorkDateAsc()
                .map(Attendance::getWorkDate)
                .orElse(null);

        return employees.stream()
                .map(e -> employeeScore(e, firstWorkDate))
                .sorted(Comparator.comparing(AttritionInsightDto::score).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttritionInsightDto getAttritionForEmployee(Long employeeId, Authentication authentication) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with employee id " + employeeId)
                );
        boolean isHrOrAdmin = hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_HR");
        boolean isTheirManager = employee.getManager() != null && employee.getManager().getEmail().equalsIgnoreCase(authentication.getName());

        if (!isHrOrAdmin && !isTheirManager) {
            throw new AccessDeniedException("You can only view your attrition risk for your own team");
        }

        LocalDate firstWorkDate = attendanceRepository.findFirstByOrderByWorkDateAsc()
                .map(Attendance::getWorkDate)
                .orElse(null);

        return employeeScore(employee, firstWorkDate);
    }

    private AttritionInsightDto employeeScore(Employee employee, LocalDate firstWorkDate) {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        int score = 0;
        List<String> reasons = new ArrayList<>();
        Double attendanceRate = attendanceRate(employee, firstWorkDate, today);
        if (attendanceRate != null && attendanceRate < 80) {
            score += 30;
            reasons.add("Attendance is " + attendanceRate + "% in recent weeks (below 80%)");
        } else if (attendanceRate != null && attendanceRate < 90) {
            score += 15;
            reasons.add("Attendance is " + attendanceRate + "% in recent weeks (below 90%)");
        }
        Integer latestRating = reviewRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId())
                .stream()
                .map(PerformanceReview::getRating)
                .findFirst()
                .orElse(null);
        if (latestRating != null && latestRating <= 2) {
            score += 25;
            reasons.add("Latest review rating is " + latestRating + " out of 5");
        } else if (latestRating != null && latestRating == 3) {
            score += 10;
            reasons.add("Latest review rating is " + latestRating + " out of 5");
        }
        List<Goal> goals = goalRepository.findByEmployeeIdOrderByDueDateAsc(employee.getId());
        List<PerformanceReview> reviews = reviewRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());

        Double goalProgress = goals.isEmpty() ? null
                : round1(goals.stream()
                .mapToInt(Goal::getProgress)
                .average()
                .orElse(0)
        );

        if (goalProgress != null && goalProgress < 40) {
            score += 20;
            reasons.add("Average goal progress is only " + goalProgress + "%");
        }

        LocalDate ninetyDaysBefore = today.minusDays(90);
        int unPlannedLeaves = leaveRequestRepository.findByEmployeeIdOrderByFromDateDesc(employee.getId())
                .stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .filter(l -> l.getLeaveType() == LeaveType.SICK || l.getLeaveType() == LeaveType.CASUAL)
                .filter(l -> !l.getFromDate().isBefore(ninetyDaysBefore))
                .mapToInt(LeaveRequest::getDays)
                .sum();
        if (unPlannedLeaves > 3) {
            score += 15;
            reasons.add(unPlannedLeaves + " days of sick/casual leaves in the last 90 days");
        }
        long tenureMonths = ChronoUnit.MONTHS.between(employee.getJoiningDate(), today);
        if (tenureMonths < 6) {
            score += 10;
            reasons.add("Joined only " + tenureMonths + " month(s) ago");
        }
        if (employee.getStatus() == EmployeeStatus.NOTICE_PERIOD) {
            score = 100;
            reasons.add(0, "Already serving notice period");
        }
        score = Math.min(score, 100);
        RiskLevel level = score >= 60 ? RiskLevel.HIGH : score >= 30 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        if (reasons.isEmpty()) {
            reasons.add("No warning signs");
        }
        return new AttritionInsightDto(
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getDepartment() == null ? null : employee.getDepartment().getName(),
                score,
                level,
                attendanceRate,
                latestRating,
                goalProgress,
                unPlannedLeaves,
                tenureMonths,
                reasons
        );
    }

    private Double attendanceRate(Employee employee, LocalDate firstWorkDate, LocalDate today) {

        if (firstWorkDate == null)
            return null;
        LocalDate yesterday = today.minusDays(1);
        LocalDate windowStart = today.minusDays(60);
        if (windowStart.isBefore(firstWorkDate)) windowStart = firstWorkDate;
        if (windowStart.isBefore(employee.getJoiningDate())) windowStart = employee.getJoiningDate();
        if (yesterday.isBefore(windowStart))
            return null;
        final LocalDate from = windowStart;
        long presentDays = attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employee.getId(), from, yesterday)
                .stream()
                .filter(a -> a.getAttendanceStatus() != AttendanceStatus.ABSENT)
                .count();
        int absent = 0;
        YearMonth month = YearMonth.from(from);
        YearMonth lastMonth = YearMonth.from(yesterday);
        while (!month.isAfter(lastMonth)) {
            absent += (int) attendanceService.findAbsentDates(employee, month)
                    .stream()
                    .filter(date -> !date.isBefore(from))
                    .count();
            month = month.plusMonths(1);
        }
        long total = presentDays + absent;

        return total == 0 ? null : round1(presentDays * 100.0 / total);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private SkillGapDto buildSkillGap(Department department, List<Employee> members) {

        Set<String> requireSkills = RequiredSkills.forDepartment(department.getName());

        Map<String, Long> peoplePerSkill = members.stream()
                .flatMap(e -> e.getSkills().stream())
                .map(skill -> skill.toLowerCase())
                .collect(Collectors.groupingBy(skill -> skill, Collectors.counting()));

        ArrayList<String> covered = new ArrayList<>();
        ArrayList<String> missing = new ArrayList<>();
        ArrayList<String> singlePerson = new ArrayList<>();

        for (String skill : requireSkills.stream().toList()) {
            long people = peoplePerSkill.getOrDefault(skill.toLowerCase(), 0L);
            if (people == 0) {
                missing.add(skill);
            } else {
                covered.add(skill);
                if (people == 1) {
                    singlePerson.add(skill);
                }
            }
        }
        int percentage = requireSkills.isEmpty() ? 100
                : (int) Math.round(covered.size() * 100.0 / requireSkills.size());

        return new SkillGapDto(
                department.getId(),
                department.getName(),
                members.size(),
                requireSkills.stream().sorted().toList(),
                covered,
                missing,
                singlePerson,
                percentage
        );
    }

    public static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
