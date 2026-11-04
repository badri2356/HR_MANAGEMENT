package com.employee.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "salary_structures")
@Getter
@Setter
@NoArgsConstructor
public class SalaryStructure extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(nullable = false)
    private BigDecimal basic;

    @Column(nullable = false)
    private BigDecimal hra;

    @Column(nullable = false)
    private BigDecimal allowances;

    @Column(nullable = false)
    private boolean pfApplicable;

}
