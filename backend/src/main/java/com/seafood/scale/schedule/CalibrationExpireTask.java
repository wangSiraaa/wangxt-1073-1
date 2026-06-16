package com.seafood.scale.schedule;

import com.seafood.scale.service.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CalibrationExpireTask {

    @Autowired
    private ScaleService scaleService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredScales() {
        scaleService.checkAndUpdateExpiredScales(-1L, "SYSTEM");
    }
}
