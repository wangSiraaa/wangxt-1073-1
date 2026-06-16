package com.seafood.scale.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class StallOperateVO {
    private Long stallId;
    private String stallCode;
    private String stallName;
    private String stallStatus;
    private String stallStatusText;
    private boolean canOperate;
    private boolean businessQualified;
    private boolean hasExpiredScale;
    private boolean hasDisabledScale;
    private boolean hasActiveSuspension;
    private int expiredScaleCount;
    private int disabledScaleCount;
    private int usableScaleCount;
    private int totalScaleCount;
    private List<String> blockReasons = new ArrayList<>();
    private List<ScaleBrief> scales = new ArrayList<>();
    private SuspensionBrief activeSuspension;

    @Data
    public static class ScaleBrief {
        private Long scaleId;
        private String scaleCode;
        private String status;
        private String statusText;
        private boolean businessQualified;
        private String calibrationExpireDate;
        private boolean expired;
    }

    @Data
    public static class SuspensionBrief {
        private Long suspensionId;
        private String suspensionType;
        private String suspensionTypeText;
        private String reason;
        private String suspendedAt;
        private String operatorName;
    }
}
