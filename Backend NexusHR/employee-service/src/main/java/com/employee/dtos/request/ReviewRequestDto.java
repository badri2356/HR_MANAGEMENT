package com.employee.dtos.request;

import com.employee.domain.enums.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {

    @NotNull(message = "Employee is required")
    private Long employeeId;

    @NotNull(message = "Review type is required")
    private ReviewType type;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(max = 2000, message = "Comments cannot exceed 2000 characters")
    private String comments;
}