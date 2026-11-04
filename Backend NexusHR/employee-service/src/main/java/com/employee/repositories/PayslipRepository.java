package com.employee.repositories;

import com.employee.domain.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByPayrollRunIdOrderByEmployeeFirstNameAsc(Long runId);

    void deleteByPayrollRunId(Long runId);

    List<Payslip> findByEmployeeId(Long runId);

}
