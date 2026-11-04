package com.employee.controllers;

import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, attendanceService.checkIn(email), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, attendanceService.checkOut(email), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyAttendance(@AuthenticationPrincipal String email, @RequestParam YearMonth month) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, attendanceService.getMyAttendance(email, month), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<?> getAttendanceByDate(@RequestParam LocalDate date) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, attendanceService.getAttendanceByDate(date), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/summary/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<?> attendanceSummary() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, attendanceService.attendanceSummary(), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }
}

