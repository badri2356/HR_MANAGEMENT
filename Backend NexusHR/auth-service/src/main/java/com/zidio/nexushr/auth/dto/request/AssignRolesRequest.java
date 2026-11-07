package com.zidio.nexushr.auth.dto.request;

import com.zidio.nexushr.auth.domain.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AssignRolesRequest {

    @NotNull(message = "At least one role required")
    private Set<RoleName> roles;
}
