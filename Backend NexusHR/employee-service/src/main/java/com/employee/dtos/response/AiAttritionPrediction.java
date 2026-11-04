package com.employee.dtos.response;

import java.util.List;

public record AiAttritionPrediction(
        List<String> reasons,
        String recommendation

) {
}
