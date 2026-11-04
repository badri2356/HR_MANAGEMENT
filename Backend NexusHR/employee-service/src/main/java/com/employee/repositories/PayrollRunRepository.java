package com.employee.repositories;

import com.employee.domain.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    boolean existsByYearAndMonth(int year, int month);

    List<PayrollRun> findAllByOrderByYearDescMonthDesc();

    Optional<PayrollRun> findFirstByOrderByYearDescMonthDesc();
}
