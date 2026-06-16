package com.seafood.scale.repository;

import com.seafood.scale.entity.Reinspection;
import com.seafood.scale.enums.ReinspectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReinspectionRepository extends JpaRepository<Reinspection, Long> {
    Reinspection findByReinspectionNo(String reinspectionNo);
    List<Reinspection> findByComplaintId(Long complaintId);
    List<Reinspection> findByStallId(Long stallId);
    List<Reinspection> findByStatus(ReinspectionStatus status);
    List<Reinspection> findByMetrologistId(Long metrologistId);
}
