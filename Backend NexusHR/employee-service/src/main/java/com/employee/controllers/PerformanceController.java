package com.employee.controllers;

import com.employee.dtos.request.GoalRequestDto;
import com.employee.dtos.request.ReviewRequestDto;
import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.PerformanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/performances")
public class PerformanceController {

    private final PerformanceService performance;

    public PerformanceController(PerformanceService performance) {
        this.performance = performance;
    }

    @PostMapping("/goals")
    public ResponseEntity<?> createGoal(@Valid @RequestBody GoalRequestDto requestDto,
                                        Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE,
                        performance.createGoal(requestDto, authentication),
                        SuccessMessages.DATA_SAVED_SUCCESSFULLY)
                );
    }

    @PatchMapping("/goals/{goalId}")
    public ResponseEntity<?> update(@PathVariable Long goalId,
                                    @Valid @RequestBody GoalRequestDto requestDto,
                                    Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        performance.updateGoal(goalId, requestDto, authentication),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY)
                );
    }

    @GetMapping("/goals/me")
    public ResponseEntity<?> getMyGoals(Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        performance.getMyGoals(authentication),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY)
                );
    }

    @GetMapping("/goals/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeGoals(@PathVariable Long employeeId,
                                              Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        performance.getEmployeeGoals(employeeId, authentication),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY)
                );
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequestDto requestDto,
                                          Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, performance.createReview(requestDto, authentication), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @GetMapping("/reviews/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeReview(@PathVariable Long employeeId,
                                               Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, performance.getEmployeeReviews(employeeId, authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/scorecards/{employeeId}")
    public ResponseEntity<?> getEmployeeScorecard(@PathVariable Long employeeId,
                                                  Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, performance.getEmployeeScorecard(employeeId, authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}
