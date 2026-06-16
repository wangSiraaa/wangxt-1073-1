package com.seafood.scale.service;

import com.seafood.scale.entity.Stall;
import com.seafood.scale.enums.StallStatus;
import com.seafood.scale.repository.StallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StallService {

    @Autowired
    private StallRepository stallRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<Stall> findAll() {
        return stallRepository.findAll();
    }

    public Stall findById(Long id) {
        return stallRepository.findById(id).orElse(null);
    }

    public Stall findByStallCode(String stallCode) {
        return stallRepository.findByStallCode(stallCode);
    }

    public List<Stall> findByStatus(StallStatus status) {
        return stallRepository.findByStatus(status);
    }

    @Transactional
    public Stall save(Stall stall, Long operatorId, String operatorName) {
        Stall saved = stallRepository.save(stall);
        auditLogService.log("CREATE", "STALL", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "创建摊位: " + saved.getStallCode(), null);
        return saved;
    }

    @Transactional
    public Stall update(Stall stall, Long operatorId, String operatorName) {
        Stall saved = stallRepository.save(stall);
        auditLogService.log("UPDATE", "STALL", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "更新摊位: " + saved.getStallCode(), null);
        return saved;
    }

    @Transactional
    public void updateBusinessQualified(Long id, Boolean qualified,
                                         Long operatorId, String operatorName) {
        Stall stall = findById(id);
        if (stall != null) {
            stall.setBusinessQualified(qualified);
            stallRepository.save(stall);
            auditLogService.log("UPDATE_BUSINESS_QUALIFIED", "STALL", id,
                    operatorId, operatorName, "ADMIN",
                    "更新摊位营业资格: " + stall.getStallCode() + " -> " + qualified, null);
        }
    }

    @Transactional
    public void delete(Long id, Long operatorId, String operatorName) {
        Stall stall = findById(id);
        if (stall != null) {
            stallRepository.deleteById(id);
            auditLogService.log("DELETE", "STALL", id,
                    operatorId, operatorName, "ADMIN",
                    "删除摊位: " + stall.getStallCode(), null);
        }
    }

    public boolean isStallOperable(Long stallId) {
        Stall stall = findById(stallId);
        if (stall == null) return false;
        if (stall.getStatus() == StallStatus.SUSPENDED || stall.getStatus() == StallStatus.CLOSED) {
            return false;
        }
        return stall.getBusinessQualified() != null && stall.getBusinessQualified();
    }
}
