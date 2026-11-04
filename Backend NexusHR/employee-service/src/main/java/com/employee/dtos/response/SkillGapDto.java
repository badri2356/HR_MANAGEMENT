package com.employee.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapDto {

    Long departmentId;
    String departmentName;
    int employeeCount; // count of working employees in department
    List<String> requiredSkills;
    List<String> coveredSkills;
    List<String> missingSkills;
    List<String> singlePersonSkills;
    int percentage;

}
