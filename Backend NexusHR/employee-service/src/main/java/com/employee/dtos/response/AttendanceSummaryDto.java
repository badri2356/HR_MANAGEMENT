package com.employee.dtos.response;

import java.time.LocalDate;

public record AttendanceSummaryDto(LocalDate date,
                                   long activeEmployee,
                                   long present,
                                   long late,
                                   long halfDay,
                                   long notCheckedIn) {
}
