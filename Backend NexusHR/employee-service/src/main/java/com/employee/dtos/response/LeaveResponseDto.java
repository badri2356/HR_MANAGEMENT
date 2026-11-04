package com.employee.dtos.response;

import com.employee.domain.enums.LeaveStatus;
import com.employee.domain.enums.LeaveType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class LeaveResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType type;
    private LeaveStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int days;
    private String reason;
    private String decidedBy;
    private Instant appliedAt;
}
