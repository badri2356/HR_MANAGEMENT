package com.employee.dtos.response;

import com.employee.domain.enums.RiskLevel;

import java.util.List;

public record AttritionInsightDto(
        Long employeeId,
        String employeeName,
        String departmentName,
        int score,                    // 0–100, higher = more likely to leave
        RiskLevel riskLevel,          // LOW, MEDIUM, HIGH
        Double attendanceRate,        // e.g. 88.2 (%); null if there's no attendance data yet
        Integer latestRating,         // most recent review rating; null if never reviewed
        Double goalProgress,          // average goal progress (%); null if no goals
        int unplannedLeaveDays,       // sick + casual leave days in the last 90 days
        long tenureMonths,            // months since joining
        List<String> reasons          // why points were added, in plain words for HR
) {
}
