package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.Reinspection;
import com.seafood.scale.enums.ReinspectionStatus;
import com.seafood.scale.service.ReinspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reinspections")
public class ReinspectionController {

    @Autowired
    private ReinspectionService reinspectionService;

    @GetMapping
    public Result<List<Reinspection>> findAll() {
        return Result.success(reinspectionService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Reinspection> findById(@PathVariable Long id) {
        return Result.success(reinspectionService.findById(id));
    }

    @GetMapping("/no/{no}")
    public Result<Reinspection> findByNo(@PathVariable String no) {
        return Result.success(reinspectionService.findByReinspectionNo(no));
    }

    @GetMapping("/stall/{stallId}")
    public Result<List<Reinspection>> findByStallId(@PathVariable Long stallId) {
        return Result.success(reinspectionService.findByStallId(stallId));
    }

    @GetMapping("/complaint/{complaintId}")
    public Result<List<Reinspection>> findByComplaintId(@PathVariable Long complaintId) {
        return Result.success(reinspectionService.findByComplaintId(complaintId));
    }

    @GetMapping("/status/{status}")
    public Result<List<Reinspection>> findByStatus(@PathVariable ReinspectionStatus status) {
        return Result.success(reinspectionService.findByStatus(status));
    }

    @PostMapping
    public Result<Reinspection> create(@RequestBody Reinspection reinspection,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                        @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(reinspectionService.create(reinspection, userId, userName));
    }

    @PutMapping("/{id}/schedule")
    public Result<Reinspection> schedule(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestHeader(value = "X-User-Name", required = false) String userName) {
        LocalDateTime scheduledTime = LocalDateTime.parse((String) body.get("scheduledTime"));
        Long metrologistId = body.get("metrologistId") != null ? Long.valueOf(body.get("metrologistId").toString()) : null;
        String metrologistName = (String) body.get("metrologistName");
        return Result.success(reinspectionService.schedule(id, scheduledTime, metrologistId, metrologistName, userId, userName));
    }

    @PutMapping("/{id}/start")
    public Result<Reinspection> start(@PathVariable Long id,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                       @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(reinspectionService.startInspection(id, userId, userName));
    }

    @PutMapping("/{id}/complete")
    public Result<Reinspection> complete(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestHeader(value = "X-User-Name", required = false) String userName) {
        BigDecimal standardWeight = body.get("standardWeight") != null ? new BigDecimal(body.get("standardWeight").toString()) : null;
        BigDecimal measuredWeight = body.get("measuredWeight") != null ? new BigDecimal(body.get("measuredWeight").toString()) : null;
        String resultRemark = (String) body.get("resultRemark");
        Boolean borrowedUsed = body.get("borrowedUsed") != null ? (Boolean) body.get("borrowedUsed") : false;
        Long borrowedScaleId = body.get("borrowedScaleId") != null ? Long.valueOf(body.get("borrowedScaleId").toString()) : null;
        return Result.success(reinspectionService.complete(id, standardWeight, measuredWeight,
                resultRemark, borrowedUsed, borrowedScaleId, userId, userName));
    }

    @PutMapping("/{id}/cancel")
    public Result<Reinspection> cancel(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                        @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(reinspectionService.cancel(id, body.get("reason"), userId, userName));
    }
}
