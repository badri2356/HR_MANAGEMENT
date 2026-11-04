package com.employee.services.service;

import com.employee.domain.entity.Employee;
import com.employee.dtos.response.AttendanceResponseDto;
import com.employee.dtos.response.AttendanceSummaryDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

    AttendanceResponseDto checkIn(String email);

    AttendanceResponseDto checkOut(String email);

    AttendanceSummaryDto attendanceSummary();

    List<AttendanceResponseDto> getMyAttendance(String email, YearMonth month);

    List<AttendanceResponseDto> getAttendanceByDate(LocalDate date);

    public List<LocalDate> findAbsentDates(Employee employee, YearMonth month);

    // True if at least one attendance record exists in the month (i.e. the month was tracked)
    public boolean hasAttendanceData(YearMonth month);
}
