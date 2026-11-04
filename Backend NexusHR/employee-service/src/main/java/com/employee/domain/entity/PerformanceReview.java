package com.employee.domain.entity;

import com.employee.domain.enums.ReviewType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "performance_review")
@Getter
@Setter
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String comments;

}
