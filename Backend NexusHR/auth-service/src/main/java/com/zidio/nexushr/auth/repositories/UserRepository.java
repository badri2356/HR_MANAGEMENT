package com.zidio.nexushr.auth.repositories;

import com.zidio.nexushr.auth.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})   // add "roles.permissions" if getAuthorities() reads permissions
    Optional<User> findWithRolesByEmail(String email);

}
