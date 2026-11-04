package com.zidio.nexushr.auth.repositories;

import com.zidio.nexushr.auth.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
