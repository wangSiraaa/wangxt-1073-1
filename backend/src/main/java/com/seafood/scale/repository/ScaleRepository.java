package com.seafood.scale.repository;

import com.seafood.scale.entity.Scale;
import com.seafood.scale.enums.ScaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScaleRepository extends JpaRepository<Scale, Long> {
    Scale findByScaleCode(String scaleCode);
    List<Scale> findByStallId(Long stallId);
    List<Scale> findByStatus(ScaleStatus status);
    List<Scale> findByIsBorrowedFalse();

    @Query("SELECT s FROM Scale s WHERE s.calibrationExpireDate < :date AND s.status != 'DISABLED'")
    List<Scale> findExpiredScales(LocalDate date);

    @Query("SELECT s FROM Scale s WHERE s.calibrationExpireDate BETWEEN :start AND :end")
    List<Scale> findScalesExpiringBetween(LocalDate start, LocalDate end);
}
