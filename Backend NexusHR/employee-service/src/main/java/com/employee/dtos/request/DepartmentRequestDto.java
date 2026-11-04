package com.employee.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentRequestDto {
    @NotNull(message = "Department name is required")
    private String name;

}
