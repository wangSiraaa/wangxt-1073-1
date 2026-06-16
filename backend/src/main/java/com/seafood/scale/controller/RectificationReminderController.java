package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.RectificationReminder;
import com.seafood.scale.service.RectificationReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rectification-reminders")
public class RectificationReminderController {

    @Autowired
    private RectificationReminderService reminderService;

    @GetMapping
    public Result<List<RectificationReminder>> findAll() {
        return Result.success(reminderService.findAll());
    }

    @GetMapping("/{id}")
    public Result<RectificationReminder> findById(@PathVariable Long id) {
        return Result.success(reminderService.findById(id));
    }

    @GetMapping("/stall/{stallId}")
    public Result<List<RectificationReminder>> findByStallId(@PathVariable Long stallId) {
        return Result.success(reminderService.findByStallId(stallId));
    }

    @GetMapping("/pending")
    public Result<List<RectificationReminder>> findPending() {
        return Result.success(reminderService.findPendingReminders());
    }

    @GetMapping("/sent")
    public Result<List<RectificationReminder>> findSent() {
        return Result.success(reminderService.findSentReminders());
    }

    @PostMapping("/process")
    public Result<Integer> processDueReminders() {
        return Result.success(reminderService.processDueReminders());
    }
}
