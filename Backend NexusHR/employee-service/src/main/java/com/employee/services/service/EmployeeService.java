package com.employee.services.service;

import com.employee.dtos.request.EmployeeRequestDto;
import com.employee.dtos.request.SkillsRequestDto;
import com.employee.dtos.response.EmployeeResponseDto;
import com.employee.dtos.response.PageResponse;
import org.hibernate.query.Page;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDto create(EmployeeRequestDto requestDto);

    PageResponse<EmployeeResponseDto> getAll(int page, int size);

    EmployeeResponseDto getById(Long id);

    EmployeeResponseDto getMe(String email);

    EmployeeResponseDto update(Long id, EmployeeRequestDto requestDto);

    EmployeeResponseDto approveEmployee(Long id);

    EmployeeResponseDto offboardingEmployee(Long id);

    EmployeeResponseDto exitEmployee(Long id, String reason);

    EmployeeResponseDto updateSkills(Long id, SkillsRequestDto request);

}
