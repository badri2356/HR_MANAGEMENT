package com.employee.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngagementRequestDto {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer jobSatisfaction;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer managerSupport;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer workLifeBalance;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer careerGrowth;
    @Size(max = 1000)
    private String comment;
}
