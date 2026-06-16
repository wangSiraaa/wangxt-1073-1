package com.seafood.scale.service;

import com.seafood.scale.dto.StallScaleUsageVO;
import com.seafood.scale.dto.StallScaleUsageVO.ScaleUsageInfo;
import com.seafood.scale.entity.Calibration;
import com.seafood.scale.entity.Complaint;
import com.seafood.scale.entity.Scale;
import com.seafood.scale.entity.ScaleBorrow;
import com.seafood.scale.entity.Stall;
import com.seafood.scale.enums.CalibrationResult;
import com.seafood.scale.enums.ComplaintStatus;
import com.seafood.scale.enums.ScaleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class StallScaleUsageService {

    @Autowired
    private StallService stallService;

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private ScaleBorrowService scaleBorrowService;

    @Autowired
    private BusinessRuleService businessRuleService;

    @Autowired
    private CalibrationService calibrationService;

    @Autowired
    private ComplaintService complaintService;

    public StallScaleUsageVO getStallScaleUsage(Long stallId) {
        Stall stall = stallService.findById(stallId);
        if (stall == null) {
            return null;
        }

        StallScaleUsageVO vo = new StallScaleUsageVO();
        vo.setStallId(stallId);
        vo.setStallCode(stall.getStallCode());
        vo.setStallName(stall.getStallName());
        vo.setCanOperate(businessRuleService.canStallUseScales(stallId));

        List<ScaleUsageInfo> scaleInfos = new ArrayList<>();

        List<Scale> originalScales = scaleService.findByStallId(stallId);
        List<ScaleBorrow> activeBorrows = scaleBorrowService.findActiveBorrowsByStall(stallId);

        for (Scale scale : originalScales) {
            ScaleUsageInfo info = buildScaleUsageInfo(scale, true, activeBorrows);
            scaleInfos.add(info);
        }

        for (ScaleBorrow borrow : activeBorrows) {
            Scale borrowedScale = scaleService.findById(borrow.getScaleId());
            if (borrowedScale != null) {
                ScaleUsageInfo info = buildBorrowedScaleUsageInfo(borrowedScale, borrow);
                scaleInfos.add(info);
            }
        }

        vo.setScales(scaleInfos);
        return vo;
    }

    private ScaleUsageInfo buildScaleUsageInfo(Scale scale, boolean isOriginal, List<ScaleBorrow> activeBorrows) {
        ScaleUsageInfo info = new ScaleUsageInfo();
        info.setScaleId(scale.getId());
        info.setScaleCode(scale.getScaleCode());
        info.setStatus(scale.getStatus().name());
        info.setStatusText(getStatusText(scale.getStatus()));
        info.setOriginal(isOriginal);
        info.setBusinessQualified(scale.getBusinessQualified());

        boolean isUsable = scale.getStatus() != ScaleStatus.DISABLED
                && scale.getStatus() != ScaleStatus.CALIBRATION_EXPIRED
                && scale.getStatus() != ScaleStatus.NEEDS_RECTIFICATION
                && scale.getBusinessQualified();
        info.setCurrentlyInUse(isUsable);

        if (!isUsable) {
            ScaleBorrow replacementBorrow = activeBorrows.stream()
                    .filter(b -> scale.getId().equals(b.getOriginalScaleId()))
                    .findFirst().orElse(null);
            if (replacementBorrow != null) {
                Scale replacement = scaleService.findById(replacementBorrow.getScaleId());
                if (replacement != null) {
                    info.setBorrowedScaleId(replacement.getId());
                    info.setBorrowedScaleCode(replacement.getScaleCode());
                }
                info.setBorrowContext(replacementBorrow.getBorrowContext());
            }
        }

        List<Complaint> complaints = complaintService.findByStallId(scale.getStallId());
        long activeComplaints = complaints.stream()
                .filter(c -> c.getScaleId().equals(scale.getId())
                        && (c.getStatus() == ComplaintStatus.PENDING
                        || c.getStatus() == ComplaintStatus.UNDER_INVESTIGATION
                        || c.getStatus() == ComplaintStatus.ESTABLISHED))
                .count();
        info.setActiveComplaintCount((int) activeComplaints);

        List<Calibration> calibrations = calibrationService.findByScaleId(scale.getId());
        long activeRectifications = calibrations.stream()
                .filter(c -> c.getResult() == CalibrationResult.RECTIFICATION_REQUIRED
                        && c.getRectificationDeadline() != null)
                .count();
        info.setActiveRectificationCount((int) activeRectifications);

        if (!isUsable && activeBorrows.stream().anyMatch(b -> scale.getId().equals(b.getOriginalScaleId()))) {
            info.setOperatingReason("原秤" + getStatusText(scale.getStatus()) + "，已借备用秤营业");
        } else if (!isUsable) {
            info.setOperatingReason("秤具" + getStatusText(scale.getStatus()) + "，无法营业");
        }

        return info;
    }

    private ScaleUsageInfo buildBorrowedScaleUsageInfo(Scale borrowedScale, ScaleBorrow borrow) {
        ScaleUsageInfo info = new ScaleUsageInfo();
        info.setScaleId(borrowedScale.getId());
        info.setScaleCode(borrowedScale.getScaleCode());
        info.setStatus(borrowedScale.getStatus().name());
        info.setStatusText("备用秤(借用)");
        info.setOriginal(false);
        info.setBusinessQualified(borrowedScale.getBusinessQualified());

        boolean isUsable = borrowedScale.getStatus() != ScaleStatus.DISABLED
                && borrowedScale.getBusinessQualified();
        info.setCurrentlyInUse(isUsable);

        if (borrow.getOriginalScaleId() != null) {
            Scale originalScale = scaleService.findById(borrow.getOriginalScaleId());
            if (originalScale != null) {
                info.setOriginalScaleId(originalScale.getId());
                info.setOriginalScaleCode(originalScale.getScaleCode());
                info.setOriginalScaleStatus(originalScale.getStatus().name());
            }
        }
        info.setBorrowContext(borrow.getBorrowContext());

        if (isUsable) {
            String reason = "备用秤替代营业";
            if (borrow.getOriginalScaleId() != null) {
                Scale originalScale = scaleService.findById(borrow.getOriginalScaleId());
                if (originalScale != null) {
                    reason = "原秤(" + originalScale.getScaleCode() + ")"
                            + getStatusText(originalScale.getStatus()) + "，使用备用秤营业";
                }
            }
            info.setOperatingReason(reason);
        }

        return info;
    }

    private String getStatusText(ScaleStatus status) {
        switch (status) {
            case NORMAL: return "正常";
            case NEEDS_RECTIFICATION: return "限期整改";
            case DISABLED: return "停用";
            case CALIBRATION_EXPIRED: return "校准过期";
            case BORROWED: return "已借出";
            default: return status.name();
        }
    }
}
