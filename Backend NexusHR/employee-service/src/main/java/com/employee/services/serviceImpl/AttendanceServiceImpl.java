package com.employee.services.serviceImpl;

import com.employee.domain.entity.Attendance;
import com.employee.domain.entity.Employee;
import com.employee.domain.enums.AttendanceStatus;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.LeaveStatus;
import com.employee.dtos.response.AttendanceResponseDto;
import com.employee.dtos.response.AttendanceSummaryDto;
import com.employee.exceptions.DuplicateResourceException;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.AttendanceRepository;
import com.employee.repositories.EmployeeRepository;
import com.employee.repositories.LeaveRequestRepository;
import com.employee.services.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final ZoneId OFFICE_ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalTime LATE_AFTER = LocalTime.of(9, 45);
    private static final int HALF_DAY = 4 * 60;

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository, LeaveRequestRepository leaveRequestRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Override
    @Transactional
    public AttendanceResponseDto checkIn(String email) {

        Employee activeEmployee = findActiveEmployee(email);
        LocalDate today = LocalDate.now(OFFICE_ZONE);
        LocalTime now = LocalTime.now(OFFICE_ZONE).withNano(0);

        if (attendanceRepository.existsByEmployeeIdAndWorkDate(activeEmployee.getId(), today)) {
            throw new DuplicateResourceException("You are already checked in today");
        }

        Attendance attendance = new Attendance();

        attendance.setEmployee(activeEmployee);
        attendance.setWorkDate(today);
        attendance.setCheckInTime(now);
        attendance.setAttendanceStatus(now.isAfter(LATE_AFTER) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

        Attendance save = attendanceRepository.save(attendance);

        return toResponse(save);
    }

    @Override
    @Transactional
    public AttendanceResponseDto checkOut(String email) {
        Employee activeEmployee = findActiveEmployee(email);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(activeEmployee.getId(), today)
                .orElseThrow(
                        () -> new IllegalStateException("You are not checked in today")
                );
        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("You are already checked out for today");
        }

        int worked = (int) Duration.between(attendance.getCheckInTime(), now).toMinutes();

        attendance.setCheckOutTime(now);
        attendance.setWorkedMinutes(worked);
        if (worked < HALF_DAY) {
            attendance.setAttendanceStatus(AttendanceStatus.HALF_DAY);
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryDto attendanceSummary() {
        LocalDate today = LocalDate.now(OFFICE_ZONE);
        long active = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long present = attendanceRepository.countByWorkDateAndAttendanceStatus(today, AttendanceStatus.PRESENT);
        long late = attendanceRepository.countByWorkDateAndAttendanceStatus(today, AttendanceStatus.LATE);
        long halfDay = attendanceRepository.countByWorkDateAndAttendanceStatus(today, AttendanceStatus.HALF_DAY);
        long notCheckedIn = Math.max(
                0, active - present - late - halfDay);


        return new AttendanceSummaryDto(today, active, present, late, halfDay, notCheckedIn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getMyAttendance(String email, YearMonth month) {

        Employee employee = employeeRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No employee profile found for " + email
                        ));

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        return attendanceRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                        employee.getId(), from, to
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate date) {

        return attendanceRepository
                .findByWorkDateOrderByCheckInTimeAsc(date)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<LocalDate> findAbsentDates(Employee employee, YearMonth month) {
        Set<LocalDate> approvedLeaveDates = leaveRequestRepository.findByEmployeeIdOrderByFromDateDesc(employee.getId())
                .stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .flatMap(l -> l.getFromDate().datesUntil(l.getToDate().plusDays(1)))
                .collect(Collectors.toSet());

        Set<LocalDate> holidays = Set.of();   // no holiday calendar yet; future work

        return findAbsentDates(employee, month, approvedLeaveDates, holidays);
    }

    // True if at least one attendance record exists in the month (i.e. the month was tracked)
    public boolean hasAttendanceData(YearMonth month) {
        return attendanceRepository.existsByWorkDateBetween(month.atDay(1), month.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public List<LocalDate> findAbsentDates(Employee employee, YearMonth month,
                                           Set<LocalDate> approvedLeaveDates, Set<LocalDate> holidays) {

        LocalDate start = month.atDay(1);
        if (employee.getJoiningDate().isAfter(start)) {
            start = employee.getJoiningDate();
        }

        LocalDate end = month.atEndOfMonth();
        LocalDate yesterday = LocalDate.now(OFFICE_ZONE).minusDays(1);
        if (yesterday.isBefore(end)) {
            end = yesterday;
        }
        if (employee.getExitDate() != null && employee.getExitDate().isBefore(end)) {
            end = employee.getExitDate();
        }
        if (end.isBefore(start)) {
            return List.of();
        }

        List<Attendance> records = attendanceRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employee.getId(), start, end);

        Set<LocalDate> recordedDates = records.stream()
                .map(Attendance::getWorkDate)
                .collect(Collectors.toSet());

        Set<LocalDate> markedAbsent = records.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.ABSENT)
                .map(Attendance::getWorkDate)
                .filter(day -> !approvedLeaveDates.contains(day))
                .filter(day -> !holidays.contains(day))
                .collect(Collectors.toSet());

        Set<LocalDate> missing = start.datesUntil(end.plusDays(1))
                .filter(day -> day.getDayOfWeek() != DayOfWeek.SATURDAY
                        && day.getDayOfWeek() != DayOfWeek.SUNDAY)
                .filter(day -> !approvedLeaveDates.contains(day))
                .filter(day -> !holidays.contains(day))
                .filter(day -> !recordedDates.contains(day))
                .collect(Collectors.toSet());

        return Stream.concat(markedAbsent.stream(), missing.stream())
                .distinct()
                .sorted()
                .toList();
    }

    private Employee findActiveEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email.toLowerCase())
                .orElseThrow(
                        () -> new ResourceNotFoundException("No employee found for " + email)
                );
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Only active employees can mark attendance");
        }
        return employee;
    }

    private AttendanceResponseDto toResponse(Attendance attendance) {
        AttendanceResponseDto response = new AttendanceResponseDto();

        response.setId(attendance.getId());
        response.setEmployeeId(attendance.getEmployee().getId());
        response.setEmployeeName(attendance.getEmployee().getFirstName() + " "
                + attendance.getEmployee().getLastName());
        response.setWorkDate(attendance.getWorkDate());
        response.setCheckInTime(attendance.getCheckInTime());
        response.setCheckOutTime(attendance.getCheckOutTime());
        response.setWorkedMinutes(attendance.getWorkedMinutes());
        response.setStatus(attendance.getAttendanceStatus());

        return response;
    }
}
