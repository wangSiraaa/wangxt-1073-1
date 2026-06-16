package com.seafood.scale.service;

import com.seafood.scale.entity.Calibration;
import com.seafood.scale.entity.RectificationReminder;
import com.seafood.scale.entity.RectificationReminder.ReminderTargetType;
import com.seafood.scale.entity.Scale;
import com.seafood.scale.repository.RectificationReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RectificationReminderService {

    @Autowired
    private RectificationReminderRepository reminderRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<RectificationReminder> findAll() {
        return reminderRepository.findAll();
    }

    public RectificationReminder findById(Long id) {
        return reminderRepository.findById(id).orElse(null);
    }

    public List<RectificationReminder> findByStallId(Long stallId) {
        return reminderRepository.findByStallId(stallId);
    }

    public List<RectificationReminder> findPendingReminders() {
        return reminderRepository.findByIsSentFalse();
    }

    public List<RectificationReminder> findSentReminders() {
        return reminderRepository.findByIsSentTrue();
    }

    @Transactional
    public void createRemindersForRectification(Calibration calibration, Scale scale) {
        if (calibration.getRectificationDeadline() == null) {
            return;
        }

        LocalDate deadline = calibration.getRectificationDeadline();
        LocalDate calibrationDate = calibration.getCalibrationDate();

        LocalDate threeDaysBefore = deadline.minusDays(3);
        LocalDate oneDayBefore = deadline.minusDays(1);
        LocalDate onDeadline = deadline;

        createIfAfterToday(calibration, scale, threeDaysBefore, deadline, ReminderTargetType.STALL_OWNER,
                "您的秤具(编号:" + scale.getScaleCode() + ")整改期限将至，截止日期：" + deadline + "，请尽快完成整改。");
        createIfAfterToday(calibration, scale, threeDaysBefore, deadline, ReminderTargetType.ADMIN,
                "摊位秤具(编号:" + scale.getScaleCode() + ")整改期限将至，截止日期：" + deadline + "，请关注整改进度。");
        createIfAfterToday(calibration, scale, oneDayBefore, deadline, ReminderTargetType.STALL_OWNER,
                "紧急提醒：您的秤具(编号:" + scale.getScaleCode() + ")整改截止日期为明天(" + deadline + ")，请立即完成整改！");
        createIfAfterToday(calibration, scale, oneDayBefore, deadline, ReminderTargetType.ADMIN,
                "紧急提醒：摊位秤具(编号:" + scale.getScaleCode() + ")整改截止日期为明天(" + deadline + ")，请关注。");
        createIfAfterToday(calibration, scale, onDeadline, deadline, ReminderTargetType.STALL_OWNER,
                "最后提醒：您的秤具(编号:" + scale.getScaleCode() + ")整改今日到期，未完成将停用处理！");
        createIfAfterToday(calibration, scale, onDeadline, deadline, ReminderTargetType.ADMIN,
                "最后提醒：摊位秤具(编号:" + scale.getScaleCode() + ")整改今日到期，请确认是否需要停用。");
    }

    private void createIfAfterToday(Calibration calibration, Scale scale,
                                     LocalDate reminderDate, LocalDate deadline,
                                     ReminderTargetType targetType, String content) {
        if (reminderDate.isAfter(calibration.getCalibrationDate()) || reminderDate.isEqual(calibration.getCalibrationDate())) {
            RectificationReminder reminder = new RectificationReminder();
            reminder.setCalibrationId(calibration.getId());
            reminder.setScaleId(scale.getId());
            reminder.setStallId(scale.getStallId());
            reminder.setTargetType(targetType);
            reminder.setReminderDate(reminderDate);
            reminder.setDeadline(deadline);
            reminder.setIsSent(false);
            reminder.setContent(content);
            reminderRepository.save(reminder);
        }
    }

    @Transactional
    public int processDueReminders() {
        LocalDate today = LocalDate.now();
        List<RectificationReminder> dueReminders = reminderRepository.findByReminderDateLessThanEqualAndIsSentFalse(today);
        int count = 0;
        for (RectificationReminder reminder : dueReminders) {
            reminder.setIsSent(true);
            reminder.setSentAt(LocalDateTime.now());
            reminderRepository.save(reminder);
            count++;
            auditLogService.log("RECTIFICATION_REMINDER", "REMINDER", reminder.getId(),
                    null, "SYSTEM", "SYSTEM",
                    "发送整改提醒: 秤具ID=" + reminder.getScaleId() + ", 对象=" + reminder.getTargetType()
                            + ", 截止日期=" + reminder.getDeadline(), null);
        }
        return count;
    }

    @Transactional
    public void cancelRemindersForScale(Long scaleId) {
        List<RectificationReminder> reminders = reminderRepository.findByScaleId(scaleId);
        for (RectificationReminder reminder : reminders) {
            if (!reminder.getIsSent()) {
                reminder.setIsSent(true);
                reminder.setSentAt(LocalDateTime.now());
                reminder.setContent("[已取消] " + reminder.getContent());
                reminderRepository.save(reminder);
            }
        }
    }
}
