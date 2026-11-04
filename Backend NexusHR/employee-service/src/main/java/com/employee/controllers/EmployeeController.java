package com.employee.controllers;

import com.employee.dtos.request.EmployeeRequestDto;
import com.employee.dtos.request.SkillsRequestDto;
import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> save(@RequestBody EmployeeRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.create(requestDto),
                        SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.getById(id),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.getMe(email),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY)
                );
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<?> getAll(@RequestParam int page,
                                    @RequestParam int size) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.getAll(page, size),
                        SuccessMessages.DATA_FETCHED_SUCCESSFULLY)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody EmployeeRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.update(id, requestDto),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY)
                );
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> approveEmployee(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.approveEmployee(id),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY)
                );
    }

    @PatchMapping("/{id}/offBoard")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> offBoardingEmployee(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.offboardingEmployee(id),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY)
                );
    }

    @PatchMapping("/{id}/exit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> exit(@PathVariable Long id, String reason) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.exitEmployee(id, reason),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY)
                );
    }

    @PutMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> updateSkills(@PathVariable Long id,
                                          @Valid @RequestBody SkillsRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE,
                        employeeService.updateSkills(id, requestDto),
                        SuccessMessages.DATA_UPDATED_SUCCESSFULLY)
                );
    }
}
