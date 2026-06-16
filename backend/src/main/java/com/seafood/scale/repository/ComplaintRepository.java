package com.seafood.scale.repository;

import com.seafood.scale.entity.Complaint;
import com.seafood.scale.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Complaint findByComplaintNo(String complaintNo);
    List<Complaint> findByStallId(Long stallId);
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByComplainantPhone(String complainantPhone);

    long countByStallIdAndStatusAndHandleTimeAfter(Long stallId, ComplaintStatus status, LocalDateTime after);
}
