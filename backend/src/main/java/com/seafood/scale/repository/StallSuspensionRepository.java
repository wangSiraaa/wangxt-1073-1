package com.seafood.scale.repository;

import com.seafood.scale.entity.StallSuspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StallSuspensionRepository extends JpaRepository<StallSuspension, Long> {
    List<StallSuspension> findByStallId(Long stallId);
    List<StallSuspension> findByStallIdAndIsActiveTrue(Long stallId);
    StallSuspension findTopByStallIdAndIsActiveTrueOrderBySuspendedAtDesc(Long stallId);
}
