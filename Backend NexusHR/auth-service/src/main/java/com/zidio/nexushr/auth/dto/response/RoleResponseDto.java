package com.zidio.nexushr.auth.dto.response;

import java.util.Set;

public record RoleResponseDto(
        Long id,
        String name,
        String description,
        Set<String> permissions
) {
}
