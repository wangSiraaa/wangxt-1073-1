package com.seafood.scale.service;

import com.seafood.scale.entity.Reinspection;
import com.seafood.scale.enums.ReinspectionStatus;
import com.seafood.scale.repository.ReinspectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReinspectionService {

    @Autowired
    private ReinspectionRepository reinspectionRepository;

    @Autowired
    private StallSuspensionService stallSuspensionService;

    @Autowired
    private AuditLogService auditLogService;

    public List<Reinspection> findAll() {
        return reinspectionRepository.findAll();
    }

    public Reinspection findById(Long id) {
        return reinspectionRepository.findById(id).orElse(null);
    }

    public Reinspection findByReinspectionNo(String reinspectionNo) {
        return reinspectionRepository.findByReinspectionNo(reinspectionNo);
    }

    public List<Reinspection> findByStallId(Long stallId) {
        return reinspectionRepository.findByStallId(stallId);
    }

    public List<Reinspection> findByComplaintId(Long complaintId) {
        return reinspectionRepository.findByComplaintId(complaintId);
    }

    public List<Reinspection> findByStatus(ReinspectionStatus status) {
        return reinspectionRepository.findByStatus(status);
    }

    @Transactional
    public Reinspection create(Reinspection reinspection, Long operatorId, String operatorName) {
        reinspection.setReinspectionNo(generateReinspectionNo());
        Reinspection saved = reinspectionRepository.save(reinspection);
        auditLogService.log("CREATE", "REINSPECTION", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "创建复检任务: " + saved.getReinspectionNo(), null);
        return saved;
    }

    @Transactional
    public Reinspection schedule(Long id, LocalDateTime scheduledTime, Long metrologistId,
                                 String metrologistName, Long operatorId, String operatorName) {
        Reinspection reinspection = findById(id);
        if (reinspection != null) {
            reinspection.setStatus(ReinspectionStatus.SCHEDULED);
            reinspection.setScheduledTime(scheduledTime);
            reinspection.setMetrologistId(metrologistId);
            reinspection.setMetrologistName(metrologistName);
            Reinspection saved = reinspectionRepository.save(reinspection);
            auditLogService.log("SCHEDULE", "REINSPECTION", id,
                    operatorId, operatorName, "ADMIN",
                    "预约复检: " + reinspection.getReinspectionNo() + ", 时间: " + scheduledTime, null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Reinspection startInspection(Long id, Long operatorId, String operatorName) {
        Reinspection reinspection = findById(id);
        if (reinspection != null) {
            reinspection.setStatus(ReinspectionStatus.IN_PROGRESS);
            reinspection.setActualTime(LocalDateTime.now());
            Reinspection saved = reinspectionRepository.save(reinspection);
            auditLogService.log("START", "REINSPECTION", id,
                    operatorId, operatorName, "METROLOGIST",
                    "开始复检: " + reinspection.getReinspectionNo(), null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Reinspection complete(Long id, BigDecimal standardWeight, BigDecimal measuredWeight,
                                 String resultRemark, Boolean borrowedUsed, Long borrowedScaleId,
                                 Long operatorId, String operatorName) {
        Reinspection reinspection = findById(id);
        if (reinspection != null) {
            reinspection.setStandardWeight(standardWeight);
            reinspection.setMeasuredWeight(measuredWeight);
            reinspection.setIsBorrowedScaleUsed(borrowedUsed);
            reinspection.setBorrowedScaleId(borrowedScaleId);
            reinspection.setResultRemark(resultRemark);

            if (standardWeight != null && measuredWeight != null && standardWeight.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal errorPct = measuredWeight.subtract(standardWeight)
                        .divide(standardWeight, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                reinspection.setErrorPercentage(errorPct);
            }

            boolean passed = isReinspectionPassed(standardWeight, measuredWeight);
            reinspection.setStatus(passed ? ReinspectionStatus.PASSED : ReinspectionStatus.FAILED);

            Reinspection saved = reinspectionRepository.save(reinspection);

            if (!passed && reinspection.getStallId() != null) {
                stallSuspensionService.suspendStall(
                        reinspection.getStallId(),
                        com.seafood.scale.enums.SuspensionType.REINSPECTION_FAILED,
                        id,
                        "REINSPECTION",
                        "复检未通过: " + reinspection.getReinspectionNo() + ", " + resultRemark,
                        operatorId, operatorName
                );
            }

            auditLogService.log("COMPLETE", "REINSPECTION", id,
                    operatorId, operatorName, "METROLOGIST",
                    "完成复检: " + reinspection.getReinspectionNo() + ", 结果: " + (passed ? "通过" : "未通过"), null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Reinspection cancel(Long id, String reason, Long operatorId, String operatorName) {
        Reinspection reinspection = findById(id);
        if (reinspection != null) {
            reinspection.setStatus(ReinspectionStatus.CANCELLED);
            reinspection.setResultRemark(reason);
            Reinspection saved = reinspectionRepository.save(reinspection);
            auditLogService.log("CANCEL", "REINSPECTION", id,
                    operatorId, operatorName, "ADMIN",
                    "取消复检: " + reinspection.getReinspectionNo() + ", 原因: " + reason, null);
            return saved;
        }
        return null;
    }

    private boolean isReinspectionPassed(BigDecimal standardWeight, BigDecimal measuredWeight) {
        if (standardWeight == null || measuredWeight == null) return false;
        BigDecimal diff = measuredWeight.subtract(standardWeight).abs();
        BigDecimal tolerance = standardWeight.multiply(new BigDecimal("0.01"));
        return diff.compareTo(tolerance) <= 0;
    }

    private String generateReinspectionNo() {
        return "REI" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
