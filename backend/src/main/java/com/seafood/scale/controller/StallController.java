package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.Stall;
import com.seafood.scale.enums.StallStatus;
import com.seafood.scale.service.BusinessRuleService;
import com.seafood.scale.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stalls")
public class StallController {

    @Autowired
    private StallService stallService;

    @Autowired
    private BusinessRuleService businessRuleService;

    @GetMapping
    public Result<List<Stall>> findAll() {
        return Result.success(stallService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Stall> findById(@PathVariable Long id) {
        return Result.success(stallService.findById(id));
    }

    @GetMapping("/code/{code}")
    public Result<Stall> findByCode(@PathVariable String code) {
        return Result.success(stallService.findByStallCode(code));
    }

    @GetMapping("/status/{status}")
    public Result<List<Stall>> findByStatus(@PathVariable StallStatus status) {
        return Result.success(stallService.findByStatus(status));
    }

    @GetMapping("/{id}/can-operate")
    public Result<Boolean> canOperate(@PathVariable Long id) {
        return Result.success(businessRuleService.canStallOperate(id));
    }

    @PostMapping
    public Result<Stall> create(@RequestBody Stall stall,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(stallService.save(stall, userId, userName));
    }

    @PutMapping("/{id}")
    public Result<Stall> update(@PathVariable Long id, @RequestBody Stall stall,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                @RequestHeader(value = "X-User-Name", required = false) String userName) {
        stall.setId(id);
        return Result.success(stallService.update(stall, userId, userName));
    }

    @PutMapping("/{id}/business-qualified")
    public Result<Void> updateBusinessQualified(@PathVariable Long id,
                                                 @RequestBody Map<String, Boolean> body,
                                                 @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                 @RequestHeader(value = "X-User-Name", required = false) String userName) {
        stallService.updateBusinessQualified(id, body.get("qualified"), userId, userName);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId,
                               @RequestHeader(value = "X-User-Name", required = false) String userName) {
        stallService.delete(id, userId, userName);
        return Result.success();
    }
}
