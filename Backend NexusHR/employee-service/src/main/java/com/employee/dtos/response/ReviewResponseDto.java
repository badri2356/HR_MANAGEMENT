package com.employee.dtos.response;

import com.employee.domain.enums.ReviewType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class ReviewResponseDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long reviewerId;
    private String reviewerName;
    private ReviewType type;
    private Integer rating;
    private String comments;
    private Instant submittedAt;
}
