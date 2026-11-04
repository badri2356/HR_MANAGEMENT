package com.employee.domain.entity;

import com.employee.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private Integer workedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 20)
    private AttendanceStatus attendanceStatus;
}
