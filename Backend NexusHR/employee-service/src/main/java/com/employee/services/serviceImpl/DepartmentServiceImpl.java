package com.employee.services.serviceImpl;

import com.employee.domain.entity.Department;
import com.employee.dtos.request.DepartmentRequestDto;
import com.employee.dtos.response.DepartmentResponseDto;
import com.employee.exceptions.DuplicateResourceException;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.DepartmentRepository;
import com.employee.repositories.EmployeeRepository;
import com.employee.services.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }


    @Override
    public DepartmentResponseDto getById(Long deptId) {

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("The department with " + deptId + " is not available"));

        return toResponse(department);
    }

    @Override
    public List<DepartmentResponseDto> getAll() {

        List<Department> all = departmentRepository.findAll();

        return all.stream().map(department -> {
                    DepartmentResponseDto response = new DepartmentResponseDto();
                    response.setId(department.getId());
                    response.setDepartmentName(department.getName());
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public DepartmentResponseDto save(DepartmentRequestDto departmentRequestDto) {

        Department department = new Department();

        String name = normalizationDepartmentName(departmentRequestDto.getName());

        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department " + name + " already exists");
        }

        department.setName(name);

        departmentRepository.save(department);

        return toResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDto update(Long id, DepartmentRequestDto request) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id " + id + " not found"));

        String name = normalizationDepartmentName(request.getName());

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Department " + name + " already exists");
        }

        department.setName(name);
        Department saved = departmentRepository.save(department);

        return toResponse(saved);
    }

    @Override
    public void deleteById(Long deptId) {

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id " + deptId + " not found"));

        long count = employeeRepository.countByDepartmentId(deptId);

        if (count > 0) {
            throw new DuplicateResourceException("Can not delete " + department.getName());
        }

        departmentRepository.delete(department);
    }

    private String normalizationDepartmentName(String name) {
        return Arrays.stream(name.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private DepartmentResponseDto toResponse(Department department) {
        DepartmentResponseDto response = new DepartmentResponseDto();
        response.setId(department.getId());
        response.setDepartmentName(department.getName());
        return response;
    }
}
