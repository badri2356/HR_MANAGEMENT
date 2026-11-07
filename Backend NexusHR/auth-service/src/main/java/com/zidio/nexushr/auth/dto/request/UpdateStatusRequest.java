package com.zidio.nexushr.auth.dto.request;

import com.zidio.nexushr.auth.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
