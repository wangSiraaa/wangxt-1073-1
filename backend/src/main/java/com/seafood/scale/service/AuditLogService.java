package com.seafood.scale.service;

import com.seafood.scale.entity.AuditLog;
import com.seafood.scale.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String operationType, String module, Long entityId,
                    Long operatorId, String operatorName, String operatorRole,
                    String detail, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setOperationType(operationType);
        log.setModule(module);
        log.setEntityId(entityId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperatorRole(operatorRole);
        log.setDetail(detail);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> findByModule(String module) {
        return auditLogRepository.findByModule(module);
    }
}
