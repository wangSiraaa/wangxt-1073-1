package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.enums.*;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enums")
public class EnumController {

    @GetMapping("/roles")
    public Result<List<String>> getRoles() {
        return Result.success(Arrays.stream(RoleType.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/stall-statuses")
    public Result<List<String>> getStallStatuses() {
        return Result.success(Arrays.stream(StallStatus.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/scale-statuses")
    public Result<List<String>> getScaleStatuses() {
        return Result.success(Arrays.stream(ScaleStatus.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/seal-statuses")
    public Result<List<String>> getSealStatuses() {
        return Result.success(Arrays.stream(SealStatus.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/calibration-results")
    public Result<List<String>> getCalibrationResults() {
        return Result.success(Arrays.stream(CalibrationResult.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/complaint-statuses")
    public Result<List<String>> getComplaintStatuses() {
        return Result.success(Arrays.stream(ComplaintStatus.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/reinspection-statuses")
    public Result<List<String>> getReinspectionStatuses() {
        return Result.success(Arrays.stream(ReinspectionStatus.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/suspension-types")
    public Result<List<String>> getSuspensionTypes() {
        return Result.success(Arrays.stream(SuspensionType.values()).map(Enum::name).collect(Collectors.toList()));
    }
}
