package com.employee.controllers;

import com.employee.dtos.request.SalaryStructureRequestDto;
import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // Create or update an employee's salary (PUT = "set it to this")
    @PutMapping("/salary/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> setSalary(@PathVariable Long employeeId,
                                       @Valid @RequestBody SalaryStructureRequestDto request) {   // @Valid runs the DTO checks
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.setSalary(employeeId, request), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    // HR/Admin can view anyone's salary; an employee only their own (checked in the service)
    @GetMapping("/salary/{employeeId}")
    public ResponseEntity<?> getSalary(@PathVariable Long employeeId, Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.getSalary(employeeId, authentication), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    // Create the run for a month: POST /api/payroll/runs?month=2026-10
    @PostMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> createRun(@RequestParam YearMonth month) {          // reads ?month= from the URL
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.createRun(month), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    // All runs, newest first. Salary totals are sensitive, so HR/Admin only.
    @GetMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> listRuns() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.listRun(), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    // One run with all its payslips
    @GetMapping("/runs/{runId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> getRun(@PathVariable Long runId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.getRun(runId), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    // DRAFT → PROCESSED. PATCH, never GET: this changes data.
    @PatchMapping("/runs/{runId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processRun(@PathVariable Long runId, @AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.processRun(runId, email), SuccessMessages.DATA_UPDATED_SUCCESSFULLY));
    }

    // PROCESSED → PAID. Only ADMIN: separation of duties (HR prepares, Admin releases the money)
    @PatchMapping("/runs/{runId}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> payRun(@PathVariable Long runId, @AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.payRun(runId, email), SuccessMessages.DATA_UPDATED_SUCCESSFULLY));
    }

    // Delete a DRAFT run (to fix something and recreate it)
    @DeleteMapping("/runs/{runId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> deleteRun(@PathVariable Long runId) {
        payrollService.deleteRun(runId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, null, SuccessMessages.DATA_DELETED_SUCCESSFULLY));
    }

    // The logged-in employee's own payslips (from PAID runs only)
    @GetMapping("/payslips/me")
    public ResponseEntity<?> myPayslips(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, payrollService.myPayslips(email), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}