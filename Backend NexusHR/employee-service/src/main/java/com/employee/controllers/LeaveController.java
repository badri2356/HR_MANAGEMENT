package com.employee.controllers;

import com.employee.dtos.request.LeaveRequestDto;
import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<?> apply(@AuthenticationPrincipal String email,
                                   @Valid @RequestBody LeaveRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.apply(email, requestDto), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @GetMapping("/me")
    public ResponseEntity<?> myLeaves(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.myLeaves(email), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/balance/me")
    public ResponseEntity<?> myBalance(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.myBalance(email), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> pending(Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.pending(authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @PatchMapping("/{leaveId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> approve(@PathVariable Long leaveId,
                                     Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.approved(leaveId, authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @PatchMapping("/{leaveId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> reject(@PathVariable Long leaveId,
                                    Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.reject(leaveId, authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @PatchMapping("/{leaveId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long leaveId,
                                    @AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, leaveService.cancel(leaveId, email), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}
