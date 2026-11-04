package com.employee.domain.entity;

import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends BaseEntity {

    @Column(unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ONBOARDING;

    @Column(nullable = false)
    private LocalDate joiningDate;

    private LocalDate exitDate;

    private String exitReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // The employee's skills, e.g. ["Java", "Spring Boot", "SQL"].
    // Stored in a separate table (employee_skills), one row per skill.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "skill", nullable = false, length = 50)
    private Set<String> skills = new HashSet<>();
}