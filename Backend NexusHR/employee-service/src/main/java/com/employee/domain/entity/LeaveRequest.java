package com.employee.domain.entity;

import com.employee.domain.enums.LeaveStatus;
import com.employee.domain.enums.LeaveType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    private String decidedBy;

}
