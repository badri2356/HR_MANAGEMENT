package com.employee.services.service;

import com.employee.dtos.request.LeaveRequestDto;
import com.employee.dtos.response.LeaveBalanceDto;
import com.employee.dtos.response.LeaveResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface LeaveService {

    LeaveResponseDto apply(String email, LeaveRequestDto request);

    List<LeaveResponseDto> myLeaves(String email);

    List<LeaveBalanceDto> myBalance(String email);

    List<LeaveResponseDto> pending(Authentication authentication);

    LeaveResponseDto approved(Long leaveId, Authentication authentication);

    LeaveResponseDto reject(Long leaveId, Authentication authentication);

    LeaveResponseDto cancel(Long leaveId, String email);

}
