package com.employee.services.service;

import com.employee.dtos.response.AdminDashboardDto;
import org.springframework.security.core.Authentication;

public interface AdminDashboardService {
    AdminDashboardDto getDashboard(Authentication authentication);
}
