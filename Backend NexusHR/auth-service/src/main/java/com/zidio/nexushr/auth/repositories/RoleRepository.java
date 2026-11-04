package com.zidio.nexushr.auth.repositories;

import com.zidio.nexushr.auth.domain.entity.Role;
import com.zidio.nexushr.auth.domain.enums.RoleName;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName (RoleName roleName);

}
