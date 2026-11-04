package com.employee.services.service;

import com.employee.dtos.request.DepartmentRequestDto;
import com.employee.dtos.response.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDto getById(Long deptId);

    List<DepartmentResponseDto> getAll();

    DepartmentResponseDto save(DepartmentRequestDto departmentRequestDto);

    DepartmentResponseDto update(Long id, DepartmentRequestDto departmentRequestDto);

    void deleteById(Long deptId);
}
