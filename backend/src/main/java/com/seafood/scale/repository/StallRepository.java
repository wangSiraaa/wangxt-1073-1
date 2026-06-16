package com.seafood.scale.repository;

import com.seafood.scale.entity.Stall;
import com.seafood.scale.enums.StallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StallRepository extends JpaRepository<Stall, Long> {
    Stall findByStallCode(String stallCode);
    List<Stall> findByStatus(StallStatus status);
    List<Stall> findByOwnerNameContaining(String ownerName);
}
