package com.employee.dtos.response;

import com.employee.domain.enums.PayrollStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PayrollRunResponseDto {
    private Long id;
    private String month;
    private PayrollStatus status;
    private int workingDays;
    private int employeeCount;
    private BigDecimal totalGross;
    private BigDecimal totalDeduction;
    private BigDecimal totalNet;
    private String processedBy;
    private String paidBy;
    private List<PayslipResponseDto> payslips;
    private List<String> skippedEmployees;

}
