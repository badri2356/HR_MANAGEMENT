package com.zidio.nexushr.auth.repositories;

import com.zidio.nexushr.auth.domain.entity.RefreshToken;
import com.zidio.nexushr.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String Token);

    void deleteByUser(User user);


}
