package com.employee.insights;

import java.util.Map;
import java.util.Set;

public final class RequiredSkills {

    private RequiredSkills() {
    }

    public static final Map<String, Set<String>> SKILLS = Map.of(
            "Engineering", Set.of("Java", "Spring Boot", "Microservices", "SQL", "React"),
            "Human Resources", Set.of("Recruitment", "Payroll", "Employee Relations", "Labor Law"),
            "Finance", Set.of("Accounting", "Taxation", "Excel")
    );

    public static Set<String> forDepartment(String department) {
        return SKILLS.getOrDefault(department, Set.of());
    }
}
