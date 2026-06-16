package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.AuditLog;
import com.seafood.scale.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public Result<List<AuditLog>> findAll() {
        return Result.success(auditLogService.findAll());
    }

    @GetMapping("/module/{module}")
    public Result<List<AuditLog>> findByModule(@PathVariable String module) {
        return Result.success(auditLogService.findByModule(module));
    }
}
