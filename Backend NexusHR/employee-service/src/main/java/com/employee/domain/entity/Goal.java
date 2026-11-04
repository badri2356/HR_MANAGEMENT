package com.employee.domain.entity;

import com.employee.domain.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Getter
@Setter
public class Goal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer progress = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ON_TRACK;

    @Column(nullable = false)
    private LocalDate dueDate;
}
