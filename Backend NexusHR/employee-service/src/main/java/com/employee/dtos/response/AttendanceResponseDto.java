package com.employee.dtos.response;

import com.employee.domain.enums.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AttendanceResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Integer workedMinutes;
    private AttendanceStatus status;
}
