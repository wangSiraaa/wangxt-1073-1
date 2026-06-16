package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.StallSuspension;
import com.seafood.scale.enums.SuspensionType;
import com.seafood.scale.service.StallSuspensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/suspensions")
public class StallSuspensionController {

    @Autowired
    private StallSuspensionService stallSuspensionService;

    @GetMapping
    public Result<List<StallSuspension>> findAll() {
        return Result.success(stallSuspensionService.findAll());
    }

    @GetMapping("/{id}")
    public Result<StallSuspension> findById(@PathVariable Long id) {
        return Result.success(stallSuspensionService.findById(id));
    }

    @GetMapping("/stall/{stallId}")
    public Result<List<StallSuspension>> findByStallId(@PathVariable Long stallId) {
        return Result.success(stallSuspensionService.findByStallId(stallId));
    }

    @GetMapping("/stall/{stallId}/active")
    public Result<StallSuspension> findActive(@PathVariable Long stallId) {
        return Result.success(stallSuspensionService.findActiveSuspension(stallId));
    }

    @GetMapping("/stall/{stallId}/is-suspended")
    public Result<Boolean> isSuspended(@PathVariable Long stallId) {
        return Result.success(stallSuspensionService.isStallSuspended(stallId));
    }

    @PostMapping
    public Result<StallSuspension> suspend(@RequestBody Map<String, Object> body,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        Long stallId = Long.valueOf(body.get("stallId").toString());
        SuspensionType type = SuspensionType.valueOf((String) body.get("suspensionType"));
        Long relatedId = body.get("relatedId") != null ? Long.valueOf(body.get("relatedId").toString()) : null;
        String relatedType = (String) body.get("relatedType");
        String reason = (String) body.get("reason");
        return Result.success(stallSuspensionService.suspendStall(stallId, type, relatedId, relatedType, reason, userId, userName));
    }

    @PutMapping("/{id}/reopen")
    public Result<StallSuspension> reopen(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(stallSuspensionService.reopenStall(id, body.get("reopenReason"), userId, userName));
    }
}
