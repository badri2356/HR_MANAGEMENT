package com.employee.services.service;

import com.employee.dtos.response.AttritionPrediction;
import org.springframework.security.core.Authentication;

public interface AttritionPredictionService {

    public AttritionPrediction predict(Long employeeId, Authentication authentication);

}
