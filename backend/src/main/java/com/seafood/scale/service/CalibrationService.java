package com.seafood.scale.service;

import com.seafood.scale.entity.Calibration;
import com.seafood.scale.entity.Scale;
import com.seafood.scale.enums.CalibrationResult;
import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.repository.CalibrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CalibrationService {

    @Autowired
    private CalibrationRepository calibrationRepository;

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private StallSuspensionService stallSuspensionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private RectificationReminderService rectificationReminderService;

    public List<Calibration> findAll() {
        return calibrationRepository.findAll();
    }

    public Calibration findById(Long id) {
        return calibrationRepository.findById(id).orElse(null);
    }

    public List<Calibration> findByScaleId(Long scaleId) {
        return calibrationRepository.findByScaleIdOrderByCalibrationDateDesc(scaleId);
    }

    @Transactional
    public Calibration save(Calibration calibration, Long operatorId, String operatorName) {
        Calibration saved = calibrationRepository.save(calibration);

        Scale scale = scaleService.findById(calibration.getScaleId());
        if (scale != null) {
            if (calibration.getNextCalibrationDate() != null) {
                scale.setCalibrationExpireDate(calibration.getNextCalibrationDate());
            }

            if (calibration.getResult() == CalibrationResult.PASS) {
                scale.setStatus(ScaleStatus.NORMAL);
                rectificationReminderService.cancelRemindersForScale(scale.getId());
            } else if (calibration.getResult() == CalibrationResult.RECTIFICATION_REQUIRED) {
                scale.setStatus(ScaleStatus.NEEDS_RECTIFICATION);
                rectificationReminderService.createRemindersForRectification(calibration, scale);
            } else if (calibration.getResult() == CalibrationResult.DISQUALIFIED) {
                scale.setStatus(ScaleStatus.DISABLED);
                if (scale.getStallId() != null) {
                    stallSuspensionService.suspendStall(
                            scale.getStallId(),
                            com.seafood.scale.enums.SuspensionType.COMPLIANT_ESTABLISHED,
                            calibration.getId(),
                            "CALIBRATION",
                            "秤具校准不合格: " + scale.getScaleCode(),
                            operatorId, operatorName
                    );
                }
            }
            scaleService.update(scale, operatorId, operatorName);
        }

        auditLogService.log("UPLOAD_CALIBRATION", "CALIBRATION", saved.getId(),
                operatorId, operatorName, "METROLOGIST",
                "上传校准结果: 秤具ID=" + calibration.getScaleId() + ", 结果=" + calibration.getResult(), null);

        return saved;
    }
}
