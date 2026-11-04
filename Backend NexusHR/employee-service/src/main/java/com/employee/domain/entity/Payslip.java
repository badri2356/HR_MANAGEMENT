package com.employee.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payslips")
@Getter
@Setter
@NoArgsConstructor
public class Payslip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int workingDays;

    @Column(nullable = false)
    private int absentDays;

    @Column(nullable = false)
    private BigDecimal basic;

    @Column(nullable = false)
    private BigDecimal allowances;

    @Column(nullable = false)
    private BigDecimal hra;

    @Column(nullable = false)
    private BigDecimal grossEarnings;

    @Column(nullable = false)
    private BigDecimal lossOfPay;

    @Column(nullable = false)
    private BigDecimal grossPay;

    @Column(nullable = false)
    private BigDecimal pf;

    @Column(nullable = false)
    private BigDecimal professionalTax;

    @Column(nullable = false)
    private BigDecimal tds;

    @Column(nullable = false)
    private BigDecimal totalDeductions;

    @Column(nullable = false)
    private BigDecimal netPay;

}
