package com.employee.dtos.response;

import com.employee.domain.enums.LeaveType;

public record LeaveBalanceDto(LeaveType leaveType,
                              int allocated,
                              long used,
                              long pending,
                              long available) {
}