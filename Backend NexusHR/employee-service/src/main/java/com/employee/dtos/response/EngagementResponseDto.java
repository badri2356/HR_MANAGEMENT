package com.employee.dtos.response;

import lombok.Getter;
import lombok.Setter;


public record EngagementResponseDto(String name,
                                    int responses,
                                    Double jobSatisfaction,
                                    Double managerSupport,
                                    Double workLifeBalance,
                                    Double careerGrowth,
                                    Double engagementPercent,
                                    boolean hidden) {
}
