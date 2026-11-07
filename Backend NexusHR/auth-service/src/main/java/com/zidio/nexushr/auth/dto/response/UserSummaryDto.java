package com.zidio.nexushr.auth.dto.response;

import com.zidio.nexushr.auth.domain.entity.Role;

import java.time.Instant;
import java.util.Set;

public record UserSummaryDto(
        Long id,
        String email,
        String status,
        Set<String> roles,
        Long employeeId,
        Instant createdAt
) {

}
