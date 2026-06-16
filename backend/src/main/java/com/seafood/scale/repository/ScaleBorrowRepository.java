package com.seafood.scale.repository;

import com.seafood.scale.entity.ScaleBorrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScaleBorrowRepository extends JpaRepository<ScaleBorrow, Long> {
    List<ScaleBorrow> findByScaleId(Long scaleId);
    List<ScaleBorrow> findByBorrowedToStallId(Long stallId);
    List<ScaleBorrow> findByIsReturnedFalse();
    ScaleBorrow findTopByScaleIdAndIsReturnedFalseOrderByBorrowedAtDesc(Long scaleId);
}
