package com.seafood.scale.dto;

import lombok.Data;
import java.util.List;

@Data
public class StallScaleUsageVO {
    private Long stallId;
    private String stallCode;
    private String stallName;
    private boolean canOperate;
    private List<ScaleUsageInfo> scales;

    @Data
    public static class ScaleUsageInfo {
        private Long scaleId;
        private String scaleCode;
        private String status;
        private String statusText;
        private boolean isOriginal;
        private boolean isCurrentlyInUse;
        private boolean businessQualified;
        private Long borrowedScaleId;
        private String borrowedScaleCode;
        private String borrowContext;
        private Long originalScaleId;
        private String originalScaleCode;
        private String originalScaleStatus;
        private int activeComplaintCount;
        private int activeRectificationCount;
        private String operatingReason;
    }
}
