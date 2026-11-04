package com.employee.controllers;

import com.employee.dtos.request.EngagementRequestDto;
import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.EngagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/engagement")
public class EngagementController {

    private final EngagementService engagementService;

    public EngagementController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    // Answer this month's pulse check
    @PostMapping("/responses")
    public ResponseEntity<?> submit(@Valid @RequestBody EngagementRequestDto request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, engagementService.submit(request, authentication),
                        SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    // My own answers: /api/engagement/me?month=2026-10
    @GetMapping("/me")
    public ResponseEntity<?> myResponse(@RequestParam YearMonth month, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(Boolean.FALSE, engagementService.myResponse(month, authentication),
                SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    // Results: /api/engagement/results?month=2026-10
    @GetMapping("/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<?> results(@RequestParam YearMonth month, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(Boolean.FALSE, engagementService.results(month, authentication),
                SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}
