package com.zidio.nexushr.auth.dto.response;

import java.util.Set;

public record MeResponseDto(
        Long id,
        String email,
        String status,
        Set<String> roles,
        Long employeeId
) {
}
