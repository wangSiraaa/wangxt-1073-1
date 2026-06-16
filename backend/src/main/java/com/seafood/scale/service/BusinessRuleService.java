package com.seafood.scale.service;

import com.seafood.scale.dto.StallOperateVO;
import com.seafood.scale.entity.Scale;
import com.seafood.scale.entity.ScaleBorrow;
import com.seafood.scale.entity.Stall;
import com.seafood.scale.entity.StallSuspension;
import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.enums.StallStatus;
import com.seafood.scale.enums.SuspensionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BusinessRuleService {

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private StallService stallService;

    @Autowired
    private StallSuspensionService stallSuspensionService;

    @Autowired
    private ScaleBorrowService scaleBorrowService;

    public boolean canStallOperate(Long stallId) {
        return getStallOperateStatus(stallId).isCanOperate();
    }

    public StallOperateVO getStallOperateStatus(Long stallId) {
        StallOperateVO vo = new StallOperateVO();
        Stall stall = stallService.findById(stallId);
        if (stall == null) {
            vo.setCanOperate(false);
            vo.getBlockReasons().add("摊位不存在");
            return vo;
        }

        vo.setStallId(stall.getId());
        vo.setStallCode(stall.getStallCode());
        vo.setStallName(stall.getStallName());
        vo.setStallStatus(stall.getStatus() != null ? stall.getStatus().name() : null);
        vo.setStallStatusText(stallStatusText(stall.getStatus()));
        vo.setBusinessQualified(stall.getBusinessQualified() != null && stall.getBusinessQualified());

        boolean canOperate = true;

        if (!stallService.isStallOperable(stallId)) {
            canOperate = false;
            if (stall.getStatus() == StallStatus.SUSPENDED) {
                vo.getBlockReasons().add("摊位已被暂停营业");
            } else if (stall.getStatus() == StallStatus.CLOSED) {
                vo.getBlockReasons().add("摊位已关闭");
            }
            if (stall.getBusinessQualified() == null || !stall.getBusinessQualified()) {
                vo.getBlockReasons().add("摊位营业资格未通过");
            }
        }

        List<Scale> scales = scaleService.findByStallId(stallId);
        vo.setTotalScaleCount(scales.size());
        int expiredCount = 0, disabledCount = 0, usableCount = 0;

        for (Scale s : scales) {
            StallOperateVO.ScaleBrief sb = new StallOperateVO.ScaleBrief();
            sb.setScaleId(s.getId());
            sb.setScaleCode(s.getScaleCode());
            sb.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
            sb.setStatusText(scaleStatusText(s.getStatus()));
            sb.setBusinessQualified(s.getBusinessQualified() != null && s.getBusinessQualified());
            if (s.getCalibrationExpireDate() != null) {
                sb.setCalibrationExpireDate(s.getCalibrationExpireDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                sb.setExpired(s.getCalibrationExpireDate().isBefore(java.time.LocalDate.now()));
            }
            vo.getScales().add(sb);

            if (s.getStatus() == ScaleStatus.CALIBRATION_EXPIRED) expiredCount++;
            if (s.getStatus() == ScaleStatus.DISABLED) disabledCount++;
            if (scaleService.isScaleUsable(s)) usableCount++;
        }

        vo.setExpiredScaleCount(expiredCount);
        vo.setDisabledScaleCount(disabledCount);
        vo.setUsableScaleCount(usableCount);
        vo.setHasExpiredScale(expiredCount > 0);
        vo.setHasDisabledScale(disabledCount > 0);

        if (expiredCount > 0) {
            canOperate = false;
            vo.getBlockReasons().add("存在 " + expiredCount + " 台秤具校准已过期，禁止营业");
        }

        if (disabledCount > 0 && usableCount == 0 && vo.getTotalScaleCount() > 0) {
            canOperate = false;
            vo.getBlockReasons().add("所有秤具已停用，禁止营业");
        }

        StallSuspension suspension = stallSuspensionService.findActiveSuspension(stallId);
        if (suspension != null) {
            canOperate = false;
            vo.setHasActiveSuspension(true);
            StallOperateVO.SuspensionBrief sbf = new StallOperateVO.SuspensionBrief();
            sbf.setSuspensionId(suspension.getId());
            sbf.setSuspensionType(suspension.getSuspensionType() != null ? suspension.getSuspensionType().name() : null);
            sbf.setSuspensionTypeText(suspensionTypeText(suspension.getSuspensionType()));
            sbf.setReason(suspension.getReason());
            if (suspension.getSuspendedAt() != null) {
                sbf.setSuspendedAt(suspension.getSuspendedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            sbf.setOperatorName(suspension.getOperatorName());
            vo.setActiveSuspension(sbf);
            if (suspension.getReason() != null && !vo.getBlockReasons().contains(suspension.getReason())) {
                vo.getBlockReasons().add("有效暂停记录: " + suspension.getReason());
            }
        }

        vo.setCanOperate(canOperate);
        return vo;
    }

    public boolean canStallUseScales(Long stallId) {
        if (!canStallOperate(stallId)) {
            return false;
        }

        for (Scale scale : scaleService.findByStallId(stallId)) {
            if (scale.getStatus() != ScaleStatus.CALIBRATION_EXPIRED
                    && scale.getStatus() != ScaleStatus.DISABLED
                    && scale.getStatus() != ScaleStatus.NEEDS_RECTIFICATION
                    && scale.getBusinessQualified()) {
                return true;
            }
        }

        List<ScaleBorrow> activeBorrows = scaleBorrowService.findActiveBorrowsByStall(stallId);
        for (ScaleBorrow borrow : activeBorrows) {
            Scale borrowedScale = scaleService.findById(borrow.getScaleId());
            if (borrowedScale != null
                    && borrowedScale.getStatus() != ScaleStatus.DISABLED
                    && borrowedScale.getBusinessQualified()) {
                return true;
            }
        }

        return false;
    }

    public void checkAndSuspendExpiredScales(Long stallId, Long operatorId, String operatorName) {
        List<Scale> scales = scaleService.findByStallId(stallId);
        boolean hasExpired = scales.stream()
                .anyMatch(s -> s.getStatus() == ScaleStatus.CALIBRATION_EXPIRED);
        if (hasExpired) {
            String scaleCodes = scales.stream()
                    .filter(s -> s.getStatus() == ScaleStatus.CALIBRATION_EXPIRED)
                    .map(Scale::getScaleCode)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            stallSuspensionService.suspendStall(
                    stallId,
                    SuspensionType.CALIBRATION_EXPIRED,
                    null,
                    "SCALE_EXPIRE_CHECK",
                    "存在校准过期秤具: " + scaleCodes + "，根据监管规则强制暂停营业",
                    operatorId, operatorName
            );
        }
    }

    private String stallStatusText(StallStatus s) {
        if (s == null) return "";
        switch (s) {
            case OPEN: return "营业中";
            case SUSPENDED: return "已暂停";
            case CLOSED: return "已关闭";
            default: return s.name();
        }
    }

    private String scaleStatusText(ScaleStatus s) {
        if (s == null) return "";
        switch (s) {
            case NORMAL: return "正常";
            case NEEDS_RECTIFICATION: return "限期整改";
            case DISABLED: return "已停用";
            case CALIBRATION_EXPIRED: return "校准过期";
            case BORROWED: return "已借出";
            default: return s.name();
        }
    }

    private String suspensionTypeText(SuspensionType t) {
        if (t == null) return "";
        switch (t) {
            case CALIBRATION_EXPIRED: return "校准过期暂停";
            case REINSPECTION_FAILED: return "复检未通过暂停";
            case COMPLIANT_ESTABLISHED: return "投诉成立暂停";
            case MANUAL: return "手动暂停";
            default: return t.name();
        }
    }
}
