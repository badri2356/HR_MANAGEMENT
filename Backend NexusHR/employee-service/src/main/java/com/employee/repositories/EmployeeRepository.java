package com.employee.repositories;

import com.employee.domain.entity.Employee;
import com.employee.domain.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find an employee using their email address
    Optional<Employee> findByEmail(String email);

    // Check whether an email address is already registered
    boolean existsByEmail(String email);


    //Count the employee of every Department
    long countByDepartmentId(Long deptId);

    @EntityGraph(attributePaths = {"department", "manager"})
    Page<Employee> findAll(Pageable pageable);

    long countByStatus(EmployeeStatus status);

    List<Employee> findByStatusIn(Collection<EmployeeStatus> statuses);

    // Same as findByStatusIn, but also loads department and skills in the same query (avoids one query per employee)
    @EntityGraph(attributePaths = {"department", "skills"})
    List<Employee> findWithSkillsByStatusIn(Collection<EmployeeStatus> statuses);

    // EmployeeRepository: a manager's team members who are currently working
    @EntityGraph(attributePaths = {"department"})
    List<Employee> findByManagerIdAndStatusIn(Long managerId, Collection<EmployeeStatus> statuses);


}