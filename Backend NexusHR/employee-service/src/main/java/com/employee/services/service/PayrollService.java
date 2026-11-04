package com.employee.services.service;

import com.employee.dtos.request.SalaryStructureRequestDto;
import com.employee.dtos.response.PayrollRunResponseDto;
import com.employee.dtos.response.PayslipResponseDto;
import com.employee.dtos.response.SalaryStructureResponseDto;
import org.springframework.security.core.Authentication;

import java.time.YearMonth;
import java.util.List;

public interface PayrollService {

    //HR/ADMIN sets an employees salary structure
    SalaryStructureResponseDto setSalary(Long employeeId, SalaryStructureRequestDto request);

    //Fetches one employees salary structure
SalaryStructureResponseDto getSalary(Long employeeId, Authentication authentication);

    //Creates a payroll run for a selected month
    PayrollRunResponseDto createRun(YearMonth month);

    //Returns all payrolls
    List<PayrollRunResponseDto> listRun();

    //Return  details of one payroll run
    PayrollRunResponseDto getRun(Long runId);

    //Calculates salaries for employees and generates their payslips
    PayrollRunResponseDto processRun(Long runId, String adminEmail);

    //After payment completed mark a payrun
    PayrollRunResponseDto payRun(Long runId, String adminEmail);

    //Delete a payrun which is in draft
    void deleteRun(Long runId);

    //Returns he logged in employee's payslips
    List<PayslipResponseDto> myPayslips(String email);

}