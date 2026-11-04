package com.employee.dtos.response;

import java.util.List;

public record EngagementResultDto(String month,
                                  EngagementResponseDto company,
                                  List<EngagementResponseDto> departments) {
}
