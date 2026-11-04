package com.employee.dtos.response;

import com.employee.domain.enums.GoalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoalResponseDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String title;
    private int progress;
    private GoalStatus status;
    private LocalDate dueDate;

}
