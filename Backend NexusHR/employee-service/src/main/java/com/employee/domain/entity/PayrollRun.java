package com.employee.domain.entity;

import com.employee.domain.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_runs")
@Getter
@Setter
@NoArgsConstructor
public class PayrollRun extends BaseEntity{

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(nullable = false)
    private int workingDays;

    @Column(nullable = false)
    private int employeeCount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;          // starts at 0; the real total is set after the payslips

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    private String processedBy;

    private String paidBy;

}
