package com.zidio.nexushr.auth.services.service;

import com.zidio.nexushr.auth.dto.request.AssignRolesRequest;
import com.zidio.nexushr.auth.dto.request.UpdateStatusRequest;
import com.zidio.nexushr.auth.dto.response.RoleResponseDto;
import com.zidio.nexushr.auth.dto.response.UserSummaryDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AdminService {

    List<RoleResponseDto> getRoles();

    List<UserSummaryDto> getUsers();

    UserSummaryDto assignRoles(Long userId, AssignRolesRequest assignRolesRequest, String adminEmail);

    UserSummaryDto updateStatus(Long userId, UpdateStatusRequest status, Authentication authentication);
}
