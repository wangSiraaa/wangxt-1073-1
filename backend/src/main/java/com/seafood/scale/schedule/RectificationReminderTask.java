package com.seafood.scale.schedule;

import com.seafood.scale.service.RectificationReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RectificationReminderTask {

    @Autowired
    private RectificationReminderService reminderService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDueReminders() {
        reminderService.processDueReminders();
    }
}
