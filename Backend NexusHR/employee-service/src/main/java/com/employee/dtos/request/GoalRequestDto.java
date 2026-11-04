package com.employee.dtos.request;

import com.employee.domain.enums.GoalStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoalRequestDto {

    private Long employeeId;


    @Size(max = 200, message = "Title can be at most 200 characters")
    private String title;

    @Min(value = 0, message = "Progress can not be below 0")
    @Max(value = 100, message = "Progress can not be above 100")
    private Integer progress;

    private GoalStatus status;

    private LocalDate dueDate;

}
