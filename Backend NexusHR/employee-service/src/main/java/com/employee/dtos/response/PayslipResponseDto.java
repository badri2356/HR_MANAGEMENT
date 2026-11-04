package com.employee.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PayslipResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private int workingDays;
    private int absentDays;
    private String month;
    private BigDecimal basic;
    private BigDecimal hra;
    private BigDecimal allowance;
    private BigDecimal grossEarnings;
    private BigDecimal lossOfPay;
    private BigDecimal grossPay;
    private BigDecimal pf;
    private BigDecimal professionalTax;
    private BigDecimal tds;
    private BigDecimal totalDeduction;
    private BigDecimal netPay;
}
