package com.employee.services.service;

import com.employee.dtos.response.AttritionInsightDto;
import com.employee.dtos.response.SkillGapDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface InsightService {
    List<SkillGapDto> getSkillGaps();

    List<AttritionInsightDto> getAttritionRisk(Authentication authentication);

    AttritionInsightDto getAttritionForEmployee(Long employeeId, Authentication authentication);
}
