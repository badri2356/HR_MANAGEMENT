package com.employee.repositories;

import com.employee.domain.entity.EngagementResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EngagementResponseRepository extends JpaRepository<EngagementResponse, Long> {
    boolean existsByEmployeeIdAndMonth(Long employeeId, String month);           // already answered this month?

    Optional<EngagementResponse> findByEmployeeIdAndMonth(Long employeeId, String month);

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    List<EngagementResponse> findByMonth(String month);
}
