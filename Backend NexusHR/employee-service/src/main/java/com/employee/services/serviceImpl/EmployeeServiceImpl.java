package com.employee.services.serviceImpl;

import com.employee.domain.entity.Department;
import com.employee.domain.entity.Employee;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.NotificationType;
import com.employee.dtos.request.EmployeeRequestDto;
import com.employee.dtos.request.SkillsRequestDto;
import com.employee.dtos.response.EmployeeResponseDto;
import com.employee.dtos.response.PageResponse;
import com.employee.events.NotificationEvent;
import com.employee.exceptions.DuplicateResourceException;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.DepartmentRepository;
import com.employee.repositories.EmployeeRepository;
import com.employee.services.service.EmployeeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, ApplicationEventPublisher eventPublisher) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @Transactional
    public EmployeeResponseDto create(EmployeeRequestDto requestDto) {

        String email = requestDto.getEmail().trim().toLowerCase();

        if (employeeRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Employee with email " + email + " already exists");
        }

        Department department = departmentRepository.findById(requestDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id " + requestDto.getDepartmentId()));

        Employee manager = null;
        if (requestDto.getManagerId() != null) {
            manager = employeeRepository.findById(requestDto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id " + requestDto.getManagerId()));
        }

        Employee employee = buildEmployee(requestDto, email, department, manager);
        Employee saved = employeeRepository.save(employee);

        return toResponse(saved);
    }


    @Override
    @Transactional
    public PageResponse<EmployeeResponseDto> getAll(int page, int size) {
//        return employeeRepository.findAll().stream().map(this::toResponse).toList();
        int safePage = Math.max(page, 0);                  // no negative pages
        int safeSize = Math.min(Math.max(size, 1), 100);   // between 1 and 100

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("firstName").ascending());

        Page<Employee> employees = employeeRepository.findAll(pageable);

        return new PageResponse<>(employees.map(this::toResponse));
    }

    @Override
    @Transactional
    public EmployeeResponseDto getById(Long id) {


        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Employee not found with id " + id)
        );

        return toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getMe(String email) {

        Employee employee = employeeRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("No employee profile found for " + email)
        );

        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponseDto update(Long id, EmployeeRequestDto requestDto) {

        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));

        String email = requestDto.getEmail().trim().toLowerCase();

        if (!employee.getEmail().equals(email)
                && employeeRepository.existsByEmail(email)) {

            throw new DuplicateResourceException(
                    "Employee with email " + email + " already exists"
            );
        }

        Department department = departmentRepository.findById(requestDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id " + requestDto.getDepartmentId()
                ));

        Employee manager = null;

        if (requestDto.getManagerId() != null) {
            manager = employeeRepository.findById(requestDto.getManagerId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Manager not found with id " + requestDto.getManagerId())
                    );
        }
        employee.setFirstName(requestDto.getFirstName().trim());
        employee.setLastName(requestDto.getLastName().trim());
        employee.setEmail(email);
        employee.setPhone(requestDto.getPhone());
        employee.setDesignation(requestDto.getDesignation());
        employee.setJoiningDate(requestDto.getJoiningDate());
        employee.setEmploymentType(requestDto.getEmploymentType());
        employee.setDepartment(department);
        employee.setManager(manager);

        Employee save = employeeRepository.save(employee);

        return toResponse(save);
    }

    @Override
    @Transactional
    public EmployeeResponseDto approveEmployee(Long id) {

        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Employee not present with id " + id
                ));

        if (employee.getStatus() != EmployeeStatus.ONBOARDING) {
            throw new IllegalStateException(
                    "Employee cannot be approved from status " + employee.getStatus()
            );
        }
        employee.setStatus(EmployeeStatus.ACTIVE);

        Employee save = employeeRepository.save(employee);

        eventPublisher.publishEvent(new NotificationEvent(
                save.getEmail(),
                NotificationType.LEAVE_APPROVED,
                "Your onboarding is complete and your profile is now active."
        ));

        return toResponse(save);
    }

    @Override
    @Transactional
    public EmployeeResponseDto offboardingEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Employee not present with id " + id
                        ));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Employee can not be offboarded from status " + employee.getStatus()
            );
        }

        employee.setStatus(EmployeeStatus.NOTICE_PERIOD);
        Employee save = employeeRepository.save(employee);

        return toResponse(save);
    }

    @Override
    @Transactional
    public EmployeeResponseDto exitEmployee(Long id, String reason) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not present with id " + id)
                );

        if (employee.getStatus() != EmployeeStatus.NOTICE_PERIOD) {
            throw new IllegalStateException(
                    "Employee can not be exited from status " + employee.getStatus()
            );
        }
        employee.setStatus(EmployeeStatus.EXITED);
        employee.setExitReason(reason);
        employee.setExitDate(LocalDate.now());

        Employee save = employeeRepository.save(employee);

        return toResponse(save);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateSkills(Long id, SkillsRequestDto request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with employeeId " + id)
                );
        Set<String> word = request.getSkills()
                .stream()
                .filter(s ->  s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());

        employee.getSkills().clear();
        employee.getSkills().addAll(word);

        return toResponse(employeeRepository.save(employee));
    }



    private Employee buildEmployee(EmployeeRequestDto requestDto, String email,
                                   Department department, Employee manager) {
        Employee employee = new Employee();
        employee.setFirstName(requestDto.getFirstName().trim());
        employee.setLastName(requestDto.getLastName().trim());
        employee.setEmail(email);
        employee.setPhone(requestDto.getPhone());
        employee.setDesignation(requestDto.getDesignation());
        employee.setEmploymentType(requestDto.getEmploymentType());
        employee.setJoiningDate(requestDto.getJoiningDate());
        employee.setDepartment(department);
        employee.setManager(manager);
        employee.setStatus(EmployeeStatus.ONBOARDING);
        employee.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 3).toUpperCase());
        return employee;
    }

    private EmployeeResponseDto toResponse(Employee employee) {
        EmployeeResponseDto response = new EmployeeResponseDto();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setUserId(employee.getUserId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setDesignation(employee.getDesignation());
        response.setEmploymentType(employee.getEmploymentType());
        response.setEmployeeStatus(employee.getStatus());
        response.setJoiningDate(employee.getJoiningDate());
        response.setExitDate(employee.getExitDate());

        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getName());
        }

        if (employee.getManager() != null) {
            response.setManagerId(employee.getManager().getId());
            response.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }

        return response;
    }

}



