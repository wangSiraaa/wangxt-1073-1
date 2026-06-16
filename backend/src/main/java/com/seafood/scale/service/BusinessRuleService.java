package com.seafood.scale.service;

import com.seafood.scale.entity.Scale;
import com.seafood.scale.entity.ScaleBorrow;
import com.seafood.scale.entity.Stall;
import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.enums.SuspensionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        Stall stall = stallService.findById(stallId);
        if (stall == null) return false;

        if (!stallService.isStallOperable(stallId)) {
            return false;
        }

        if (stallSuspensionService.isStallSuspended(stallId)) {
            return false;
        }

        return true;
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
        for (Scale scale : scaleService.findByStallId(stallId)) {
            if (scale.getStatus() == ScaleStatus.CALIBRATION_EXPIRED) {
                stallSuspensionService.suspendStall(
                        stallId,
                        SuspensionType.CALIBRATION_EXPIRED,
                        scale.getId(),
                        "SCALE",
                        "秤具校准过期: " + scale.getScaleCode(),
                        operatorId, operatorName
                );
                break;
            }
        }
    }
}
