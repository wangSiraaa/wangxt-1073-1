package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.Calibration;
import com.seafood.scale.service.CalibrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/calibrations")
public class CalibrationController {

    @Autowired
    private CalibrationService calibrationService;

    @GetMapping
    public Result<List<Calibration>> findAll() {
        return Result.success(calibrationService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Calibration> findById(@PathVariable Long id) {
        return Result.success(calibrationService.findById(id));
    }

    @GetMapping("/scale/{scaleId}")
    public Result<List<Calibration>> findByScaleId(@PathVariable Long scaleId) {
        return Result.success(calibrationService.findByScaleId(scaleId));
    }

    @PostMapping
    public Result<Calibration> create(@RequestBody Calibration calibration,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                       @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(calibrationService.save(calibration, userId, userName));
    }
}
