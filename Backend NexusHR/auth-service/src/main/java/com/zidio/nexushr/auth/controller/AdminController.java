package com.zidio.nexushr.auth.controller;

import com.zidio.nexushr.auth.dto.request.AssignRolesRequest;
import com.zidio.nexushr.auth.dto.request.UpdateStatusRequest;
import com.zidio.nexushr.auth.dto.response.ApiResponse;
import com.zidio.nexushr.auth.messages.SuccessMessages;
import com.zidio.nexushr.auth.services.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        adminService.getRoles(),
                        SuccessMessages.ROLES_FETCHED_SUCCESSFULLY)
                );
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        adminService.getUsers(),
                        SuccessMessages.USERS_FETCHED_SUCCESSFULLY)
                );
    }

    @PatchMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoles(@PathVariable Long id,
                                         @Valid @RequestBody AssignRolesRequest request,
                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        adminService.assignRoles(id, request, authentication.getName()),
                        SuccessMessages.ROLES_ASSIGNED_SUCCESSFULLY)
                );
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateStatusRequest request,
                                          Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        adminService.updateStatus(id, request, authentication),
                        SuccessMessages.STATUS_UPDATED_SUCCESSFULLY)
                );
    }
}
