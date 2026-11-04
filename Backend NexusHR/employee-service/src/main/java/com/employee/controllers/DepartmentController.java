package com.employee.controllers;

import com.employee.dtos.request.DepartmentRequestDto;
import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping()
    public ResponseEntity<?> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, departmentService.getAll(), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, departmentService.getById(id), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> save(@Valid @RequestBody DepartmentRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(Boolean.FALSE, departmentService.save(requestDto), SuccessMessages.DATA_SAVED_SUCCESSFULLY));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid
                                    @RequestBody DepartmentRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, departmentService.update(id, requestDto), SuccessMessages.DATA_UPDATED_SUCCESSFULLY));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        departmentService.deleteById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, null, SuccessMessages.DATA_DELETED_SUCCESSFULLY));
    }
}