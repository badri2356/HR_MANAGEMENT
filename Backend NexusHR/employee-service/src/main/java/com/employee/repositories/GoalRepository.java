package com.employee.repositories;

import com.employee.domain.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    //find all goals of one employee with ascending order
    List<Goal> findByEmployeeIdOrderByDueDateAsc(Long employeeId);
}
