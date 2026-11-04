package com.employee.controllers;

import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.AttritionPredictionService;
import com.employee.services.service.InsightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {
    private final InsightService insightService;
    private final AttritionPredictionService attritionPredictionService;

    public InsightsController(InsightService insightService, AttritionPredictionService attritionPredictionService) {
        this.insightService = insightService;
        this.attritionPredictionService = attritionPredictionService;
    }

    @GetMapping("/skill-gaps")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getSkillGaps() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        insightService.getSkillGaps(),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY)
                );
    }

    @GetMapping("/attrition")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getAttrition(Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, insightService.getAttritionRisk(authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
    @GetMapping("/attrition/{employeeId}/prediction")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<?> predictAttrition(@PathVariable Long employeeId, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(Boolean.FALSE,
                attritionPredictionService.predict(employeeId, authentication),
                SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}
