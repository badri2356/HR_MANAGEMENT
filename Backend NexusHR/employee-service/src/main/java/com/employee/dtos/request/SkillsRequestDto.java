package com.employee.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SkillsRequestDto {
    @NotNull(message = "Skills list is required")
    private Set<String>skills;
}
