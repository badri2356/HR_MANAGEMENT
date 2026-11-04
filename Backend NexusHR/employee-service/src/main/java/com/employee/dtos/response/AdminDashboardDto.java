package com.employee.dtos.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminDashboardDto(
        Headcount headCount,
        AttendanceSummaryDto attendanceToday,
        long pendingLeaveRequests,
        LatestPayroll latestPayroll,
        AttritionSummary attritionSummary,
        List<SkillGapDto> departmentsWithGaps, // at least one missing department
        Double engagementPercent  // this month company score
) {
    public record Headcount(
            long total,
            Map<String, Long> byStatus,
            Map<String, Long> byDepartment
    ) {
    }

    public record LatestPayroll(
            String month,
            String status,
            BigDecimal totalNet,
            int employeeCount
    ) {
    }

    public record AttritionSummary(
            long high,
            long medium,
            long low, List<AttritionInsightDto> topAtRisk
    ) {
    }
}

