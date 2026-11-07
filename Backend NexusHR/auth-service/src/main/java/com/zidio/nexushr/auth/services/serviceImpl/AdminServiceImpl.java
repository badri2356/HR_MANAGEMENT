package com.zidio.nexushr.auth.services.serviceImpl;

import com.zidio.nexushr.auth.domain.entity.Role;
import com.zidio.nexushr.auth.domain.entity.User;
import com.zidio.nexushr.auth.domain.enums.RoleName;
import com.zidio.nexushr.auth.domain.enums.UserStatus;
import com.zidio.nexushr.auth.dto.request.AssignRolesRequest;
import com.zidio.nexushr.auth.dto.request.UpdateStatusRequest;
import com.zidio.nexushr.auth.dto.response.RoleResponseDto;
import com.zidio.nexushr.auth.dto.response.UserSummaryDto;
import com.zidio.nexushr.auth.exceptions.ResourceNotFoundException;
import com.zidio.nexushr.auth.repositories.RefreshTokenRepository;
import com.zidio.nexushr.auth.repositories.RoleRepository;
import com.zidio.nexushr.auth.repositories.UserRepository;
import com.zidio.nexushr.auth.services.service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AdminServiceImpl(RoleRepository roleRepository,
                            UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getRoles() {
        // It will give you all the roles from database sorted as per the name.

        return roleRepository.findAll()
                .stream()
                .map(
                        role -> new RoleResponseDto(
                                role.getId(),
                                role.getName().name(),
                                role.getDescription(),
                                role.getPermissions().stream()
                                        .map(permission -> permission.getCode().name())
                                        .collect(Collectors.toCollection(TreeSet::new))
                        )
                ).sorted(Comparator.comparing(RoleResponseDto::name))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getEmail))
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public UserSummaryDto assignRoles(Long userId, AssignRolesRequest request, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        boolean itSelf = user.getEmail().equalsIgnoreCase(adminEmail);
        if (itSelf && !request.getRoles().contains(RoleName.ADMIN)) {
            throw new IllegalArgumentException("You cannot remove your own ADMIN role.");
        }

        Set<Role> newRoles = new HashSet<>();
        for (RoleName name : request.getRoles()) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
            newRoles.add(role);
        }
        user.getRoles().clear();
        user.getRoles().addAll(newRoles);
        User save = userRepository.save(user);

        return toSummary(save);
    }

    @Override
    @Transactional
    public UserSummaryDto updateStatus(Long userId, UpdateStatusRequest status, Authentication authentication) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        String callerEmail = authentication.getName();

        if (user.getEmail().equalsIgnoreCase(callerEmail)) {
            throw new IllegalArgumentException("You cannot change your own status.");
        }
        boolean callerIsAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean targetIsAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);
        if (!callerIsAdmin && targetIsAdmin) {
            throw new IllegalArgumentException("You cannot change the status of an ADMIN user.");
        }

        user.setStatus(status.getStatus());
        User save = userRepository.save(user);
        if (user.getStatus() != UserStatus.ACTIVE) {
            refreshTokenRepository.deleteByUser(user);
        }
        return toSummary(save);
    }

    private UserSummaryDto toSummary(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toCollection(TreeSet::new));
        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getStatus().name(),
                roles,
                user.getEmployeeId(),
                user.getCreatedAt()
        );
    }
}
