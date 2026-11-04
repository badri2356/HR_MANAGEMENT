package com.employee.services.service;

import com.employee.dtos.request.GoalRequestDto;
import com.employee.dtos.request.ReviewRequestDto;
import com.employee.dtos.response.GoalResponseDto;
import com.employee.dtos.response.ReviewResponseDto;
import com.employee.dtos.response.ScoreCard;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PerformanceService {

    // Goal operations
    GoalResponseDto createGoal(GoalRequestDto request, Authentication authentication);

    GoalResponseDto updateGoal(Long goalId, GoalRequestDto request, Authentication authentication);

    List<GoalResponseDto> getMyGoals(Authentication authentication);

    List<GoalResponseDto> getEmployeeGoals(Long employeeId, Authentication authentication);

    // Performance review operations
    ReviewResponseDto createReview(ReviewRequestDto request, Authentication authentication);

    List<ReviewResponseDto> getEmployeeReviews(Long employeeId, Authentication authentication);

    // Employee scorecard
    ScoreCard getEmployeeScorecard(Long employeeId, Authentication authentication);
}
