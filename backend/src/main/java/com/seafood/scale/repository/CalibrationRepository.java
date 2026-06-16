package com.seafood.scale.repository;

import com.seafood.scale.entity.Calibration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CalibrationRepository extends JpaRepository<Calibration, Long> {
    List<Calibration> findByScaleIdOrderByCalibrationDateDesc(Long scaleId);
    List<Calibration> findByMetrologistId(Long metrologistId);
}
