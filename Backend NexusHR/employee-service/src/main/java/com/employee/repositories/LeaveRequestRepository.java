package com.employee.repositories;

import com.employee.domain.entity.LeaveRequest;
import com.employee.domain.enums.LeaveStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    //Gets an employee's leave history, newest first.
    List<LeaveRequest> findByEmployeeIdOrderByFromDateDesc(Long employeeId);

    //Gets pending requests in creation order
    @EntityGraph(attributePaths = "employee")
    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status);

    //Gets leave requests for employees managed by a particular manager.
    @EntityGraph(attributePaths = "employee")
    List<LeaveRequest> findByEmployeeManagerIdAndStatusOrderByCreatedAtDesc(Long managerId, LeaveStatus status);

    //count of particular status
    long countByStatus(LeaveStatus status);
}
