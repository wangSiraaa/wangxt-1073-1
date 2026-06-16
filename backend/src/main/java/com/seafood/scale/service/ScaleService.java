package com.seafood.scale.service;

import com.seafood.scale.entity.Scale;
import com.seafood.scale.entity.Stall;
import com.seafood.scale.entity.StallSuspension;
import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.enums.StallStatus;
import com.seafood.scale.enums.SuspensionType;
import com.seafood.scale.repository.ScaleRepository;
import com.seafood.scale.repository.StallRepository;
import com.seafood.scale.repository.StallSuspensionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScaleService {

    @Autowired
    private ScaleRepository scaleRepository;

    @Autowired
    private StallRepository stallRepository;

    @Autowired
    private StallSuspensionRepository stallSuspensionRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<Scale> findAll() {
        return scaleRepository.findAll();
    }

    public Scale findById(Long id) {
        return scaleRepository.findById(id).orElse(null);
    }

    public Scale findByScaleCode(String scaleCode) {
        return scaleRepository.findByScaleCode(scaleCode);
    }

    public List<Scale> findByStallId(Long stallId) {
        return scaleRepository.findByStallId(stallId);
    }

    public List<Scale> findAvailableScales() {
        return scaleRepository.findByIsBorrowedFalse();
    }

    @Transactional
    public Scale save(Scale scale, Long operatorId, String operatorName) {
        boolean newlyExpired = false;
        if (scale.getCalibrationExpireDate() != null &&
                scale.getCalibrationExpireDate().isBefore(LocalDate.now())) {
            if (scale.getStatus() != ScaleStatus.CALIBRATION_EXPIRED) {
                newlyExpired = true;
            }
            scale.setStatus(ScaleStatus.CALIBRATION_EXPIRED);
        }
        Scale saved = scaleRepository.save(scale);
        auditLogService.log("CREATE", "SCALE", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "登记秤具: " + saved.getScaleCode(), null);

        if (saved.getStallId() != null && saved.getStatus() == ScaleStatus.CALIBRATION_EXPIRED) {
            forceSuspendStallOnExpired(saved.getStallId(), operatorId, operatorName);
        } else if (newlyExpired && saved.getStallId() != null) {
            suspendStallIfNoUsableScale(saved.getStallId(), operatorId, operatorName);
        }
        return saved;
    }

    @Transactional
    public Scale update(Scale scale, Long operatorId, String operatorName) {
        Scale saved = scaleRepository.save(scale);
        auditLogService.log("UPDATE", "SCALE", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "更新秤具: " + saved.getScaleCode(), null);
        return saved;
    }

    @Transactional
    public void bindStall(Long scaleId, Long stallId, Long operatorId, String operatorName) {
        Scale scale = findById(scaleId);
        if (scale != null) {
            scale.setStallId(stallId);
            scaleRepository.save(scale);
            auditLogService.log("BIND_STALL", "SCALE", scaleId,
                    operatorId, operatorName, "ADMIN",
                    "秤具绑定摊位: " + scale.getScaleCode() + " -> 摊位ID:" + stallId, null);

            if (scale.getStatus() == ScaleStatus.CALIBRATION_EXPIRED) {
                forceSuspendStallOnExpired(stallId, operatorId, operatorName);
            } else if (scale.getStatus() == ScaleStatus.DISABLED) {
                suspendStallIfNoUsableScale(stallId, operatorId, operatorName);
            }
        }
    }

    @Transactional
    public void updateBusinessQualified(Long id, Boolean qualified,
                                        Long operatorId, String operatorName) {
        Scale scale = findById(id);
        if (scale != null) {
            scale.setBusinessQualified(qualified);
            if (!qualified) {
                scale.setStatus(ScaleStatus.DISABLED);
            }
            scaleRepository.save(scale);
            auditLogService.log("UPDATE_BUSINESS_QUALIFIED", "SCALE", id,
                    operatorId, operatorName, "ADMIN",
                    "更新秤具营业资格: " + scale.getScaleCode() + " -> " + qualified, null);

            if (!qualified && scale.getStallId() != null) {
                suspendStallIfNoUsableScale(scale.getStallId(), operatorId, operatorName);
            }
        }
    }

    @Transactional
    public Map<String, Object> checkAndUpdateExpiredScales(Long operatorId, String operatorName) {
        List<Scale> expired = scaleRepository.findExpiredScales(LocalDate.now());
        Set<Long> affectedStallIds = new HashSet<>();
        List<Map<String, Object>> affectedStalls = new ArrayList<>();
        int expiredCount = 0;

        for (Scale s : expired) {
            boolean newlyExpired = false;
            if (s.getStatus() != ScaleStatus.DISABLED && s.getStatus() != ScaleStatus.NEEDS_RECTIFICATION
                    && s.getStatus() != ScaleStatus.CALIBRATION_EXPIRED) {
                s.setStatus(ScaleStatus.CALIBRATION_EXPIRED);
                scaleRepository.save(s);
                newlyExpired = true;
                expiredCount++;
            }
            if (s.getStallId() != null) {
                affectedStallIds.add(s.getStallId());
            }
        }

        final Long finalOpId = operatorId == null ? 1L : operatorId;
        final String finalOpName = operatorName == null ? "系统任务" : operatorName;

        for (Long stallId : affectedStallIds) {
            StallSuspension suspended = forceSuspendStallOnExpired(stallId, finalOpId, finalOpName);
            if (suspended != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("stallId", stallId);
                Stall stall = stallRepository.findById(stallId).orElse(null);
                info.put("stallCode", stall != null ? stall.getStallCode() : null);
                info.put("stallName", stall != null ? stall.getStallName() : null);
                info.put("suspensionId", suspended.getId());
                info.put("reason", suspended.getReason());
                info.put("stallStatus", stall != null ? (stall.getStatus() != null ? stall.getStatus().name() : null) : null);
                affectedStalls.add(info);
            } else {
                Stall stall = stallRepository.findById(stallId).orElse(null);
                if (stall != null && stallSuspensionRepository.findByStallIdAndIsActiveTrue(stallId).size() > 0) {
                    StallSuspension existing = stallSuspensionRepository
                            .findTopByStallIdAndIsActiveTrueOrderBySuspendedAtDesc(stallId);
                    Map<String, Object> info = new HashMap<>();
                    info.put("stallId", stallId);
                    info.put("stallCode", stall.getStallCode());
                    info.put("stallName", stall.getStallName());
                    info.put("suspensionId", existing != null ? existing.getId() : null);
                    info.put("reason", existing != null ? existing.getReason() : null);
                    info.put("stallStatus", stall.getStatus() != null ? stall.getStatus().name() : null);
                    info.put("note", "已存在有效暂停记录，跳过重复创建");
                    affectedStalls.add(info);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("expiredScaleCount", expiredCount);
        result.put("alreadyExpiredCount", expired.size());
        result.put("suspendedStallCount", affectedStalls.size());
        result.put("suspendedStalls", affectedStalls);
        return result;
    }

    @Transactional
    public void delete(Long id, Long operatorId, String operatorName) {
        Scale scale = findById(id);
        if (scale != null) {
            scaleRepository.deleteById(id);
            auditLogService.log("DELETE", "SCALE", id,
                    operatorId, operatorName, "ADMIN",
                    "删除秤具: " + scale.getScaleCode(), null);
        }
    }

    public boolean isScaleUsable(Long scaleId) {
        Scale scale = findById(scaleId);
        return isScaleUsable(scale);
    }

    public boolean isScaleUsable(Scale scale) {
        if (scale == null) return false;
        if (scale.getStatus() == ScaleStatus.DISABLED ||
                scale.getStatus() == ScaleStatus.CALIBRATION_EXPIRED ||
                scale.getStatus() == ScaleStatus.BORROWED ||
                scale.getStatus() == ScaleStatus.NEEDS_RECTIFICATION) {
            return false;
        }
        return scale.getBusinessQualified() != null && scale.getBusinessQualified();
    }

    public boolean hasUsableScale(Long stallId) {
        if (stallId == null) return false;
        List<Scale> scales = findByStallId(stallId);
        if (scales == null || scales.isEmpty()) return false;
        for (Scale s : scales) {
            if (isScaleUsable(s)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getUnusableReasons(Long stallId) {
        List<String> reasons = new ArrayList<>();
        if (stallId == null) {
            reasons.add("摊位ID为空");
            return reasons;
        }
        List<Scale> scales = findByStallId(stallId);
        if (scales == null || scales.isEmpty()) {
            reasons.add("该摊位下未绑定任何秤具");
            return reasons;
        }
        long expired = scales.stream()
                .filter(s -> s.getStatus() == ScaleStatus.CALIBRATION_EXPIRED).count();
        long disabled = scales.stream()
                .filter(s -> s.getStatus() == ScaleStatus.DISABLED).count();
        long rect = scales.stream()
                .filter(s -> s.getStatus() == ScaleStatus.NEEDS_RECTIFICATION).count();
        long borrowed = scales.stream()
                .filter(s -> s.getStatus() == ScaleStatus.BORROWED).count();
        long unqualified = scales.stream()
                .filter(s -> s.getBusinessQualified() == null || !s.getBusinessQualified()).count();
        if (expired > 0) reasons.add(expired + "台秤具校准已过期");
        if (disabled > 0) reasons.add(disabled + "台秤具已停用");
        if (rect > 0) reasons.add(rect + "台秤具待整改");
        if (borrowed > 0) reasons.add(borrowed + "台秤具已借出");
        if (unqualified > 0) reasons.add(unqualified + "台秤具营业资格未达标");
        return reasons;
    }

    StallSuspension forceSuspendStallOnExpired(Long stallId, Long operatorId, String operatorName) {
        if (stallId == null) return null;
        if (stallSuspensionRepository.findByStallIdAndIsActiveTrue(stallId).size() > 0) {
            return null;
        }
        Stall stall = stallRepository.findById(stallId).orElse(null);
        if (stall == null) return null;
        if (stall.getStatus() == StallStatus.CLOSED) return null;

        List<String> reasons = getUnusableReasons(stallId);
        String reasonStr = "秤具校准过期强制暂停: " + String.join("; ", reasons);

        StallSuspension suspension = new StallSuspension();
        suspension.setStallId(stallId);
        suspension.setSuspensionType(SuspensionType.CALIBRATION_EXPIRED);
        suspension.setRelatedType("FORCE_SUSPEND_ON_EXPIRED");
        suspension.setReason(reasonStr);
        suspension.setOperatorId(operatorId);
        suspension.setOperatorName(operatorName);
        suspension.setIsActive(true);
        StallSuspension saved = stallSuspensionRepository.save(suspension);

        stall.setStatus(StallStatus.SUSPENDED);
        stallRepository.save(stall);

        auditLogService.log("FORCE_SUSPEND_ON_EXPIRED", "SUSPENSION", saved.getId(),
                operatorId, operatorName, "SYSTEM",
                "根据校准过期监管规则强制暂停摊位: " + stall.getStallCode() + ", 原因=" + reasonStr, null);
        return saved;
    }

    StallSuspension suspendStallIfNoUsableScale(Long stallId, Long operatorId, String operatorName) {
        if (stallId == null) return null;
        if (hasUsableScale(stallId)) {
            return null;
        }
        if (stallSuspensionRepository.findByStallIdAndIsActiveTrue(stallId).size() > 0) {
            return null;
        }
        Stall stall = stallRepository.findById(stallId).orElse(null);
        if (stall == null) return null;
        if (stall.getStatus() == StallStatus.CLOSED) return null;

        List<String> reasons = getUnusableReasons(stallId);
        String reasonStr = "秤具全部不可用: " + String.join("; ", reasons);

        StallSuspension suspension = new StallSuspension();
        suspension.setStallId(stallId);
        suspension.setSuspensionType(SuspensionType.CALIBRATION_EXPIRED);
        suspension.setRelatedType("SCALE_EXPIRE_CHECK");
        suspension.setReason(reasonStr);
        suspension.setOperatorId(operatorId);
        suspension.setOperatorName(operatorName);
        suspension.setIsActive(true);
        StallSuspension saved = stallSuspensionRepository.save(suspension);

        stall.setStatus(StallStatus.SUSPENDED);
        stallRepository.save(stall);

        auditLogService.log("AUTO_SUSPEND", "SUSPENSION", saved.getId(),
                operatorId, operatorName, "SYSTEM",
                "自动暂停摊位: " + stall.getStallCode() + ", 原因=" + reasonStr, null);
        return saved;
    }
}
