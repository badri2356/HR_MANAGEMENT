package com.zidio.nexushr.auth.repositories;

import com.zidio.nexushr.auth.domain.entity.PasswordResetToken;
import com.zidio.nexushr.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String Token);

    @Modifying // Tells Spring Data JPA that a custom @Query performs UPDATE or DELETE.
    @Transactional // Executes the database modification within a transaction.
    void deleteByUser(User user);
}
