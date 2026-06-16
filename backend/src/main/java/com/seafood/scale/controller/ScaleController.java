package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.Scale;
import com.seafood.scale.service.BusinessRuleService;
import com.seafood.scale.service.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scales")
public class ScaleController {

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private BusinessRuleService businessRuleService;

    @GetMapping
    public Result<List<Scale>> findAll() {
        return Result.success(scaleService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Scale> findById(@PathVariable Long id) {
        return Result.success(scaleService.findById(id));
    }

    @GetMapping("/code/{code}")
    public Result<Scale> findByCode(@PathVariable String code) {
        return Result.success(scaleService.findByScaleCode(code));
    }

    @GetMapping("/stall/{stallId}")
    public Result<List<Scale>> findByStallId(@PathVariable Long stallId) {
        return Result.success(scaleService.findByStallId(stallId));
    }

    @GetMapping("/available")
    public Result<List<Scale>> findAvailable() {
        return Result.success(scaleService.findAvailableScales());
    }

    @GetMapping("/{id}/usable")
    public Result<Boolean> isUsable(@PathVariable Long id) {
        return Result.success(scaleService.isScaleUsable(id));
    }

    @PostMapping
    public Result<Scale> create(@RequestBody Scale scale,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(scaleService.save(scale, userId, userName));
    }

    @PutMapping("/{id}")
    public Result<Scale> update(@PathVariable Long id, @RequestBody Scale scale,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                @RequestHeader(value = "X-User-Name", required = false) String userName) {
        scale.setId(id);
        return Result.success(scaleService.update(scale, userId, userName));
    }

    @PutMapping("/{id}/bind-stall")
    public Result<Void> bindStall(@PathVariable Long id,
                                   @RequestBody Map<String, Long> body,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestHeader(value = "X-User-Name", required = false) String userName) {
        scaleService.bindStall(id, body.get("stallId"), userId, userName);
        return Result.success();
    }

    @PutMapping("/{id}/business-qualified")
    public Result<Void> updateBusinessQualified(@PathVariable Long id,
                                                 @RequestBody Map<String, Boolean> body,
                                                 @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                 @RequestHeader(value = "X-User-Name", required = false) String userName) {
        scaleService.updateBusinessQualified(id, body.get("qualified"), userId, userName);
        return Result.success();
    }

    @PostMapping("/check-expired")
    public Result<Map<String, Object>> checkExpired(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        Long operatorId = userId != null ? userId : -1L;
        String operatorName = userName != null && !userName.isEmpty() ? userName : "SYSTEM";
        return Result.success(scaleService.checkAndUpdateExpiredScales(operatorId, operatorName));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId,
                               @RequestHeader(value = "X-User-Name", required = false) String userName) {
        scaleService.delete(id, userId, userName);
        return Result.success();
    }
}
