package com.seafood.scale.repository;

import com.seafood.scale.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByModule(String module);
    List<AuditLog> findByModuleAndEntityId(String module, Long entityId);
    List<AuditLog> findByEntityId(Long entityId);
    List<AuditLog> findByOperatorId(Long operatorId);
}
