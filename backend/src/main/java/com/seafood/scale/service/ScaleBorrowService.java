package com.seafood.scale.service;

import com.seafood.scale.entity.Scale;
import com.seafood.scale.entity.ScaleBorrow;
import com.seafood.scale.enums.ScaleStatus;
import com.seafood.scale.repository.ScaleBorrowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScaleBorrowService {

    @Autowired
    private ScaleBorrowRepository scaleBorrowRepository;

    @Autowired
    private ScaleService scaleService;

    @Autowired
    private AuditLogService auditLogService;

    public List<ScaleBorrow> findAll() {
        return scaleBorrowRepository.findAll();
    }

    public ScaleBorrow findById(Long id) {
        return scaleBorrowRepository.findById(id).orElse(null);
    }

    public List<ScaleBorrow> findByScaleId(Long scaleId) {
        return scaleBorrowRepository.findByScaleId(scaleId);
    }

    public List<ScaleBorrow> findByBorrowedToStallId(Long stallId) {
        return scaleBorrowRepository.findByBorrowedToStallId(stallId);
    }

    public List<ScaleBorrow> findActiveBorrows() {
        return scaleBorrowRepository.findByIsReturnedFalse();
    }

    @Transactional
    public ScaleBorrow borrowScale(Long scaleId, Long toStallId, Long borrowerId,
                                   String borrowerName, String borrowReason,
                                   LocalDateTime expectedReturnAt,
                                   Long operatorId, String operatorName) {
        Scale scale = scaleService.findById(scaleId);
        if (scale == null) {
            throw new RuntimeException("秤具不存在");
        }
        if (scale.getIsBorrowed()) {
            throw new RuntimeException("秤具已被借出");
        }

        ScaleBorrow borrow = new ScaleBorrow();
        borrow.setScaleId(scaleId);
        borrow.setBorrowedToStallId(toStallId);
        borrow.setBorrowerId(borrowerId);
        borrow.setBorrowerName(borrowerName);
        borrow.setBorrowReason(borrowReason);
        borrow.setExpectedReturnAt(expectedReturnAt);
        borrow.setIsReturned(false);
        ScaleBorrow saved = scaleBorrowRepository.save(borrow);

        scale.setIsBorrowed(true);
        if (scale.getStatus() != ScaleStatus.DISABLED) {
            scale.setStatus(ScaleStatus.BORROWED);
        }
        scaleService.update(scale, operatorId, operatorName);

        auditLogService.log("BORROW", "SCALE_BORROW", saved.getId(),
                operatorId, operatorName, "ADMIN",
                "借出秤具: " + scale.getScaleCode() + " 到摊位ID:" + toStallId, null);
        return saved;
    }

    @Transactional
    public ScaleBorrow returnScale(Long borrowId, String returnRemark,
                                   Long operatorId, String operatorName) {
        ScaleBorrow borrow = findById(borrowId);
        if (borrow != null && !borrow.getIsReturned()) {
            borrow.setIsReturned(true);
            borrow.setReturnedAt(LocalDateTime.now());
            borrow.setReturnRemark(returnRemark);
            ScaleBorrow saved = scaleBorrowRepository.save(borrow);

            Scale scale = scaleService.findById(borrow.getScaleId());
            if (scale != null) {
                scale.setIsBorrowed(false);
                if (scale.getStatus() == ScaleStatus.BORROWED) {
                    scale.setStatus(ScaleStatus.NORMAL);
                }
                scaleService.update(scale, operatorId, operatorName);
            }

            auditLogService.log("RETURN", "SCALE_BORROW", borrowId,
                    operatorId, operatorName, "ADMIN",
                    "归还秤具: 秤具ID=" + borrow.getScaleId(), null);
            return saved;
        }
        return null;
    }
}
