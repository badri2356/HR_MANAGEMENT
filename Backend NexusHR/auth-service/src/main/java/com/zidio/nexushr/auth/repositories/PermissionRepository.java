package com.zidio.nexushr.auth.repositories;

import com.zidio.nexushr.auth.domain.entity.Permission;
import com.zidio.nexushr.auth.domain.enums.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(PermissionCode permissionCode);
}
