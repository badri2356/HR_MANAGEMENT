
package com.employee.repositories;

import com.employee.domain.entity.Attendance;
import com.employee.domain.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByEmployeeIdAndWorkDate(
            Long employeeId,
            LocalDate workDate
    );

    Optional<Attendance> findByEmployeeIdAndWorkDate(
            Long employeeId,
            LocalDate workDate
    );

    List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    @EntityGraph(attributePaths = {"employee"})
    List<Attendance> findByWorkDateOrderByCheckInTimeAsc(
            LocalDate workDate
    );

    long countByWorkDateAndAttendanceStatus(
            LocalDate workDate,
            AttendanceStatus status
    );
    //any employee have an attendance record in this date range
    boolean existsByWorkDateBetween(
            LocalDate from,
            LocalDate to
    );
    // first attendance record in the system (when tracking began)
    Optional<Attendance> findFirstByOrderByWorkDateAsc();
}
