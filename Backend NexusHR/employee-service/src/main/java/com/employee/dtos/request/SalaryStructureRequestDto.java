package com.employee.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureRequestDto {
    @NotNull(message = "Basic salary is required")
    private BigDecimal basic;

    @NotNull(message = "HRA is required (use 0 if none)")
    private BigDecimal hra;

    @NotNull(message = "Allowances are required (use 0 if none)")
    private BigDecimal allowances;

    private Boolean pfApplicable;


}
