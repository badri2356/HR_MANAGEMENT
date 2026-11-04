package com.employee.dtos.response;

import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.EmploymentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeResponseDto {

    private Long id;

    private Long userId;

    private String firstName;

    private String lastName;

    private String employeeCode;

    private String phone;

    private String email;

    private String designation;

    private EmploymentType employmentType;

    private EmployeeStatus employeeStatus;

    private LocalDate joiningDate;

    private LocalDate exitDate;

    private Long departmentId;

    private String departmentName;

    private Long managerId;

    private String managerName;

}
