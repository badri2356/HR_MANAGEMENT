package com.employee.services.serviceImpl;

import com.employee.domain.entity.Employee;
import com.employee.domain.entity.LeaveRequest;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.LeaveStatus;
import com.employee.domain.enums.LeaveType;
import com.employee.domain.enums.NotificationType;
import com.employee.dtos.request.LeaveRequestDto;
import com.employee.dtos.response.LeaveBalanceDto;
import com.employee.dtos.response.LeaveResponseDto;
import com.employee.events.NotificationEvent;
import com.employee.exceptions.DuplicateResourceException;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.EmployeeRepository;
import com.employee.repositories.LeaveRequestRepository;
import com.employee.services.service.LeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class LeaveRequestServiceImpl implements LeaveService {

    private static final ZoneId OFFICE_ZONE = ZoneId.of("Asia/Kolkata");

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository, ApplicationEventPublisher eventPublisher) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @Transactional
    public LeaveResponseDto apply(String email, LeaveRequestDto request) {
        Employee employee = employeeRepository.findByEmail(email.toLowerCase())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with email:" + email)
                );
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Only active employees can apply for leave");
        }

        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new IllegalStateException("To date can not be before from date");
        }
        if (request.getFromDate().isBefore(LocalDate.now(OFFICE_ZONE))) {
            throw new IllegalStateException("From date never be a past date");
        }

        int totalDays = countWorkingDays(request.getFromDate(), request.getToDate());
        if (totalDays == 0) {
            throw new IllegalStateException("The selected days are only weekend");
        }
        List<LeaveRequest> existingLeave = leaveRequestRepository.findByEmployeeIdOrderByFromDateDesc(employee.getId());

        boolean overlaps = existingLeave.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.PENDING || leave.getStatus() == LeaveStatus.APPROVED)
                .anyMatch(leave -> !leave.getFromDate().isAfter(request.getToDate())
                        && !leave.getToDate().isBefore(request.getFromDate())
                );
        if (overlaps) {
            throw new DuplicateResourceException("You already applied leaves for some of these days");
        }
        int year = request.getFromDate().getYear();
        long pendingCount = sumDays(existingLeave, request.getLeaveType(), LeaveStatus.PENDING, year);
        long usedCount = sumDays(existingLeave, request.getLeaveType(), LeaveStatus.APPROVED, year);
        long available = request.getLeaveType().getYearlyQuota() - pendingCount - usedCount;

        if (totalDays > available) {
            throw new IllegalStateException("Not enough " + request.getLeaveType() + " leave: requested " + totalDays + " day(s),available " + available);
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setLeaveType(request.getLeaveType());
        leave.setEmployee(employee);
        leave.setDays(totalDays);
        leave.setReason(request.getReason() == null ? null : request.getReason().trim());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setFromDate(request.getFromDate());
        leave.setToDate(request.getToDate());
        LeaveRequest save = leaveRequestRepository.save(leave);

        if (employee.getManager() != null) {
            eventPublisher.publishEvent(new NotificationEvent(                 // ← publishEvent actually called
                    employee.getManager().getEmail(),
                    NotificationType.LEAVE_APPLIED,
                    employee.getFirstName() + " " +
                            employee.getLastName() + " applied for " +
                            save.getLeaveType() + " leave from " +
                            save.getFromDate() + " to " + save.getToDate() + ".")
            );
        }

        return toResponse(save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDto> myLeaves(String email) {
        Employee employee = employeeRepository.findByEmail(email.toLowerCase())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with with email " + email)
                );
        return leaveRequestRepository.findByEmployeeIdOrderByFromDateDesc(employee.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> myBalance(String email) {

        Employee employee = employeeRepository.findByEmail(email.toLowerCase())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with email " + email)
                );
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeIdOrderByFromDateDesc(employee.getId());
        int year = LocalDate.now(OFFICE_ZONE).getYear();

        return Arrays.stream(LeaveType.values())
                .map(type -> {
                            long pendingLeaves = sumDays(leaves, type, LeaveStatus.PENDING, year);
                            long approvedLeaves = sumDays(leaves, type, LeaveStatus.APPROVED, year);
                            long available = type.getYearlyQuota() - approvedLeaves - pendingLeaves;
                            return new LeaveBalanceDto(type, type.getYearlyQuota(), approvedLeaves, pendingLeaves, available);
                        }
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDto> pending(Authentication authentication) {
        List<LeaveRequest> requests;

        if (isHrOrAdmin(authentication)) {
            requests = leaveRequestRepository.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING);
        } else if (hasRole(authentication, "ROLE_MANAGER")) {
            Employee manager = employeeRepository.findByEmail(authentication.getName())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Manager not found with email " + authentication.getName())
                    );
            requests = leaveRequestRepository.findByEmployeeManagerIdAndStatusOrderByCreatedAtDesc(
                    manager.getId(), LeaveStatus.PENDING);
        } else {
            throw new AccessDeniedException("You are not allowed to view pending leave requests");
        }

        return requests.stream().map(this::toResponse).toList();
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    @Override
    @Transactional
    public LeaveResponseDto approved(Long leaveId, Authentication authentication) {
        return decide(leaveId, authentication, LeaveStatus.APPROVED);
    }

    @Override
    @Transactional
    public LeaveResponseDto reject(Long leaveId, Authentication authentication) {
        return decide(leaveId, authentication, LeaveStatus.REJECTED);
    }

    private LeaveResponseDto decide(Long leaveId, Authentication authentication, LeaveStatus decision) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Leave id does not exists")
                );
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException(("Only pending leave can be" + decision.name().toLowerCase()));
        }

        String caller = authentication.getName();
        Employee applicant = leave.getEmployee();

        if (applicant.getEmail().equalsIgnoreCase(caller)) {
            throw new AccessDeniedException("You cannot approve or reject your own leave");
        }
        boolean isManager = applicant.getManager() != null
                && applicant.getManager().getEmail().equalsIgnoreCase(caller);
        if (!isHrOrAdmin(authentication) && !isManager) {
            throw new AccessDeniedException("Only HR or Manager can decide the leave");
        }
        leave.setStatus(decision);
        leave.setDecidedBy(caller);
        LeaveRequest save = leaveRequestRepository.save(leave);

        boolean approved = decision == LeaveStatus.APPROVED;
        eventPublisher.publishEvent(new NotificationEvent(
                applicant.getEmail(),
                approved ? NotificationType.LEAVE_APPROVED : NotificationType.LEAVE_REJECTED,
                "Your " + save.getLeaveType() + " leave from " + save.getFromDate() + " to " + save.getToDate() + " has been"
                        + (approved ? " approved" : " rejected") + "."
        ));
        return toResponse(save);
    }

    @Override
    @Transactional
    public LeaveResponseDto cancel(Long leaveId, String email) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(("Id not found with id " + leaveId))
                );
        if (!leave.getEmployee().getEmail().equalsIgnoreCase(email.toLowerCase())) {
            throw new AccessDeniedException("Only you can cancel your own leave");
        }

        boolean startsLater = leave.getFromDate().isAfter(LocalDate.now(OFFICE_ZONE));
        boolean cancelable =
                (leave.getStatus() == LeaveStatus.PENDING)
                        || (leave.getStatus() == LeaveStatus.APPROVED && startsLater);
        if (!cancelable) {
            throw new IllegalStateException("This leave can't be cancelled");
        }
        leave.setStatus(LeaveStatus.CANCELLED);
        return toResponse(leaveRequestRepository.save(leave));
    }

    private boolean isHrOrAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_HR") ||
                                a.getAuthority().equals("ROLE_ADMIN"));
    }

    private static int countWorkingDays(LocalDate from, LocalDate to) {
        long count = from.datesUntil(to.plusDays(1))
                .filter(day -> day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
        return (int) count;
    }

    private static long sumDays(List<LeaveRequest> leaves, LeaveType type, LeaveStatus status, int year) {
        return leaves.stream()
                .filter(leave -> leave.getLeaveType() == type)
                .filter(leave -> leave.getStatus() == status)
                .filter(leave -> leave.getFromDate().getYear() == year)
                .mapToLong(LeaveRequest::getDays)
                .sum();
    }

    private LeaveResponseDto toResponse(LeaveRequest request) {
        LeaveResponseDto response = new LeaveResponseDto();
        response.setDays(request.getDays());
        response.setReason(request.getReason());
        response.setStatus(request.getStatus());
        response.setType(request.getLeaveType());
        response.setEmployeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
        response.setFromDate(request.getFromDate());
        response.setToDate(request.getToDate());
        response.setId(request.getId());
        response.setEmployeeId(request.getEmployee().getId());
        response.setDecidedBy(request.getDecidedBy());
        response.setAppliedAt(request.getCreatedAt());

        return response;
    }
}
