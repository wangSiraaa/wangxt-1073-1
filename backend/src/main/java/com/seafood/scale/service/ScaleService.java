package com.seafood.scale.service;

import com.seafood.scale.entity.Scale;
import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.repository.ScaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScaleService {

    @Autowired
    private ScaleRepository scaleRepository;

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
        if (scale.getCalibrationExpireDate() != null &&
                scale.getCalibrationExpireDate().isBefore(LocalDate.now())) {
            scale.setStatus(ScaleStatus.CALIBRATION_EXPIRED);
        }
        Scale saved = scaleRepository.save(scale);
        auditLogService.log("CREATE", "SCALE", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "登记秤具: " + saved.getScaleCode(), null);
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
        }
    }

    @Transactional
    public void checkAndUpdateExpiredScales() {
        List<Scale> expired = scaleRepository.findExpiredScales(LocalDate.now());
        for (Scale s : expired) {
            if (s.getStatus() != ScaleStatus.DISABLED && s.getStatus() != ScaleStatus.NEEDS_RECTIFICATION) {
                s.setStatus(ScaleStatus.CALIBRATION_EXPIRED);
                scaleRepository.save(s);
            }
        }
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
        if (scale == null) return false;
        if (scale.getStatus() == ScaleStatus.DISABLED ||
                scale.getStatus() == ScaleStatus.CALIBRATION_EXPIRED ||
                scale.getStatus() == ScaleStatus.BORROWED) {
            return false;
        }
        return scale.getBusinessQualified() != null && scale.getBusinessQualified();
    }
}
