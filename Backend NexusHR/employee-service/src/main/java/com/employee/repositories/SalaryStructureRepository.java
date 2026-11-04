package com.employee.repositories;

import com.employee.domain.entity.SalaryStructure;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    Optional<SalaryStructure> findByEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = "employee")
    List<SalaryStructure> findByEmployeeIdIn(Collection<Long> employeeIds);

}