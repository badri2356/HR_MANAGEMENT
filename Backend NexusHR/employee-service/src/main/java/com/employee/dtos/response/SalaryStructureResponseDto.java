package com.employee.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalaryStructureResponseDto {
    private Long employeeId;
    private String employeeName;
    private BigDecimal basic;
    private BigDecimal hra;
    private BigDecimal allowances;
    private BigDecimal grossMonthly;     // basic + hra + allowances
    private boolean pfApplicable;
}
