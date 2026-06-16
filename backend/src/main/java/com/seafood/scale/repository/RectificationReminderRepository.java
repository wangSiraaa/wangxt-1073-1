package com.seafood.scale.repository;

import com.seafood.scale.entity.RectificationReminder;
import com.seafood.scale.entity.RectificationReminder.ReminderTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RectificationReminderRepository extends JpaRepository<RectificationReminder, Long> {
    List<RectificationReminder> findByCalibrationId(Long calibrationId);
    List<RectificationReminder> findByScaleId(Long scaleId);
    List<RectificationReminder> findByStallId(Long stallId);
    List<RectificationReminder> findByIsSentFalse();
    List<RectificationReminder> findByIsSentTrue();
    List<RectificationReminder> findByReminderDateLessThanEqualAndIsSentFalse(LocalDate date);
    List<RectificationReminder> findByTargetTypeAndIsSentFalse(ReminderTargetType targetType);
}
