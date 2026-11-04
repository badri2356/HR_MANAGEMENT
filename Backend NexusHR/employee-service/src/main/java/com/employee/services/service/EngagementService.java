package com.employee.services.service;

import com.employee.dtos.request.EngagementRequestDto;
import com.employee.dtos.response.EngagementResponseDto;
import com.employee.dtos.response.EngagementResultDto;
import org.springframework.security.core.Authentication;

import java.time.YearMonth;

public interface EngagementService {

    public EngagementResponseDto submit(EngagementRequestDto request, Authentication authentication);

    public EngagementResponseDto myResponse(YearMonth month, Authentication authentication);

    public EngagementResultDto results(YearMonth month, Authentication authentication);
}
