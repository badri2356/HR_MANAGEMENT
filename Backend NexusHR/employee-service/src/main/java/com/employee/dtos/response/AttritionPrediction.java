package com.employee.dtos.response;

import com.employee.domain.enums.RiskLevel;

import java.util.List;

public record AttritionPrediction(
        Long employeeId,
        String employeeName,
        RiskLevel riskLevel,
        List<String> reasons,
        String recommendation,
        String source
) {
}
