package com.seafood.scale.service;

import com.seafood.scale.entity.Stall;
import com.seafood.scale.entity.StallSuspension;
import com.seafood.scale.enums.StallStatus;
import com.seafood.scale.enums.SuspensionType;
import com.seafood.scale.repository.StallSuspensionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StallSuspensionService {

    @Autowired
    private StallSuspensionRepository stallSuspensionRepository;

    @Autowired
    private StallService stallService;

    @Autowired
    private AuditLogService auditLogService;

    public List<StallSuspension> findAll() {
        return stallSuspensionRepository.findAll();
    }

    public StallSuspension findById(Long id) {
        return stallSuspensionRepository.findById(id).orElse(null);
    }

    public List<StallSuspension> findByStallId(Long stallId) {
        return stallSuspensionRepository.findByStallId(stallId);
    }

    public StallSuspension findActiveSuspension(Long stallId) {
        return stallSuspensionRepository.findTopByStallIdAndIsActiveTrueOrderBySuspendedAtDesc(stallId);
    }

    @Transactional
    public StallSuspension suspendStall(Long stallId, SuspensionType suspensionType,
                                        Long relatedId, String relatedType, String reason,
                                        Long operatorId, String operatorName) {
        StallSuspension suspension = new StallSuspension();
        suspension.setStallId(stallId);
        suspension.setSuspensionType(suspensionType);
        suspension.setRelatedId(relatedId);
        suspension.setRelatedType(relatedType);
        suspension.setReason(reason);
        suspension.setOperatorId(operatorId);
        suspension.setOperatorName(operatorName);
        suspension.setIsActive(true);
        StallSuspension saved = stallSuspensionRepository.save(suspension);

        Stall stall = stallService.findById(stallId);
        if (stall != null) {
            stall.setStatus(StallStatus.SUSPENDED);
            stallService.update(stall, operatorId, operatorName);
        }

        auditLogService.log("SUSPEND", "SUSPENSION", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "暂停摊位营业: 摊位ID=" + stallId + ", 原因=" + reason, null);
        return saved;
    }

    @Transactional
    public StallSuspension reopenStall(Long suspensionId, String reopenReason,
                                       Long operatorId, String operatorName) {
        StallSuspension suspension = findById(suspensionId);
        if (suspension != null && suspension.getIsActive()) {
            suspension.setIsActive(false);
            suspension.setReopenedById(operatorId);
            suspension.setReopenedByName(operatorName);
            suspension.setReopenReason(reopenReason);
            suspension.setReopenedAt(LocalDateTime.now());
            StallSuspension saved = stallSuspensionRepository.save(suspension);

            Stall stall = stallService.findById(suspension.getStallId());
            if (stall != null) {
                boolean hasOtherActiveSuspension = stallSuspensionRepository
                        .findByStallIdAndIsActiveTrue(stall.getId()).size() > 0;
                if (!hasOtherActiveSuspension) {
                    stall.setStatus(StallStatus.OPEN);
                    stallService.update(stall, operatorId, operatorName);
                }
            }

            auditLogService.log("REOPEN", "SUSPENSION", suspensionId,
                    operatorId, operatorName, "ADMIN",
                    "恢复摊位营业: 暂停记录ID=" + suspensionId + ", 原因=" + reopenReason, null);
            return saved;
        }
        return null;
    }

    public boolean isStallSuspended(Long stallId) {
        return stallSuspensionRepository.findByStallIdAndIsActiveTrue(stallId).size() > 0;
    }
}
