package com.employee.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "engagement_responses")
@Getter
@Setter
@NoArgsConstructor
public class EngagementResponse extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 7)
    private String month;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer jobSatisfaction;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer managerSupport;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer workLifeBalance;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer careerGrowth;

    @Column(length = 1000)
    private String comment;
}
