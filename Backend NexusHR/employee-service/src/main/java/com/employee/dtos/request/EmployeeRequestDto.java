package com.employee.dtos.request;

import com.employee.domain.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9 ]{10,15}$")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100)
    private String designation;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Department id is required")
    private Long departmentId;

    private Long managerId;

}
