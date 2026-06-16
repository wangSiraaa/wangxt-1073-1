package com.seafood.scale.service;

import com.seafood.scale.entity.Complaint;
import com.seafood.scale.entity.Reinspection;
import com.seafood.scale.enums.ComplaintStatus;
import com.seafood.scale.enums.ReinspectionStatus;
import com.seafood.scale.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReinspectionService reinspectionService;

    @Autowired
    private AuditLogService auditLogService;

    public List<Complaint> findAll() {
        return complaintRepository.findAll();
    }

    public Complaint findById(Long id) {
        return complaintRepository.findById(id).orElse(null);
    }

    public Complaint findByComplaintNo(String complaintNo) {
        return complaintRepository.findByComplaintNo(complaintNo);
    }

    public List<Complaint> findByStallId(Long stallId) {
        return complaintRepository.findByStallId(stallId);
    }

    public List<Complaint> findByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatus(status);
    }

    @Transactional
    public Complaint create(Complaint complaint, Long operatorId, String operatorName) {
        complaint.setComplaintNo(generateComplaintNo());
        complaint.setStatus(ComplaintStatus.PENDING);
        Complaint saved = complaintRepository.save(complaint);
        auditLogService.log("CREATE", "COMPLAINT", saved.getId(),
                operatorId, operatorName, "COMPLAINT_DESK",
                "受理投诉: " + saved.getComplaintNo(), null);
        return saved;
    }

    @Transactional
    public Complaint handleInvestigation(Long id, Long handlerId, String handlerName) {
        Complaint complaint = findById(id);
        if (complaint != null) {
            complaint.setStatus(ComplaintStatus.UNDER_INVESTIGATION);
            complaint.setHandlerId(handlerId);
            complaint.setHandlerName(handlerName);
            Complaint saved = complaintRepository.save(complaint);
            auditLogService.log("START_INVESTIGATION", "COMPLAINT", id,
                    handlerId, handlerName, "COMPLAINT_DESK",
                    "开始调查投诉: " + complaint.getComplaintNo(), null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Complaint establishComplaint(Long id, String handleResult,
                                         Long operatorId, String operatorName) {
        Complaint complaint = findById(id);
        if (complaint != null) {
            complaint.setStatus(ComplaintStatus.ESTABLISHED);
            complaint.setHandleResult(handleResult);
            complaint.setHandleTime(LocalDateTime.now());
            Complaint saved = complaintRepository.save(complaint);

            Reinspection reinspection = new Reinspection();
            reinspection.setComplaintId(id);
            reinspection.setStallId(complaint.getStallId());
            reinspection.setScaleId(complaint.getScaleId());
            reinspection.setStatus(ReinspectionStatus.PENDING);
            Reinspection savedReinspection = reinspectionService.create(reinspection, operatorId, operatorName);
            saved.setReinspectionId(savedReinspection.getId());
            complaintRepository.save(saved);

            auditLogService.log("ESTABLISH_COMPLAINT", "COMPLAINT", id,
                    operatorId, operatorName, "COMPLAINT_DESK",
                    "投诉成立，生成复检任务: " + complaint.getComplaintNo(), null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Complaint rejectComplaint(Long id, String handleResult,
                                     Long operatorId, String operatorName) {
        Complaint complaint = findById(id);
        if (complaint != null) {
            complaint.setStatus(ComplaintStatus.REJECTED);
            complaint.setHandleResult(handleResult);
            complaint.setHandleTime(LocalDateTime.now());
            Complaint saved = complaintRepository.save(complaint);
            auditLogService.log("REJECT_COMPLAINT", "COMPLAINT", id,
                    operatorId, operatorName, "COMPLAINT_DESK",
                    "驳回投诉: " + complaint.getComplaintNo(), null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Complaint cancelComplaint(Long id, String reason,
                                     Long operatorId, String operatorName) {
        Complaint complaint = findById(id);
        if (complaint != null) {
            complaint.setStatus(ComplaintStatus.CANCELLED);
            complaint.setHandleResult("投诉撤销: " + reason);
            complaint.setHandleTime(LocalDateTime.now());
            Complaint saved = complaintRepository.save(complaint);
            auditLogService.log("CANCEL_COMPLAINT", "COMPLAINT", id,
                    operatorId, operatorName, "COMPLAINT_DESK",
                    "撤销投诉: " + complaint.getComplaintNo() + ", 原因: " + reason, null);
            return saved;
        }
        return null;
    }

    @Transactional
    public Complaint closeComplaint(Long id, Long operatorId, String operatorName) {
        Complaint complaint = findById(id);
        if (complaint != null) {
            complaint.setStatus(ComplaintStatus.CLOSED);
            Complaint saved = complaintRepository.save(complaint);
            auditLogService.log("CLOSE_COMPLAINT", "COMPLAINT", id,
                    operatorId, operatorName, "COMPLAINT_DESK",
                    "关闭投诉: " + complaint.getComplaintNo(), null);
            return saved;
        }
        return null;
    }

    private String generateComplaintNo() {
        return "CMP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
