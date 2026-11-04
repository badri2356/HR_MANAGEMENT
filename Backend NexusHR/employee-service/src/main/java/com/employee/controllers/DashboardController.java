package com.employee.controllers;

import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.AdminDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AdminDashboardService adminDashboardService;

    public DashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, adminDashboardService.getDashboard(authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}
