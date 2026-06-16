package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.Complaint;
import com.seafood.scale.enums.ComplaintStatus;
import com.seafood.scale.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping
    public Result<List<Complaint>> findAll() {
        return Result.success(complaintService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Complaint> findById(@PathVariable Long id) {
        return Result.success(complaintService.findById(id));
    }

    @GetMapping("/no/{no}")
    public Result<Complaint> findByNo(@PathVariable String no) {
        return Result.success(complaintService.findByComplaintNo(no));
    }

    @GetMapping("/stall/{stallId}")
    public Result<List<Complaint>> findByStallId(@PathVariable Long stallId) {
        return Result.success(complaintService.findByStallId(stallId));
    }

    @GetMapping("/status/{status}")
    public Result<List<Complaint>> findByStatus(@PathVariable ComplaintStatus status) {
        return Result.success(complaintService.findByStatus(status));
    }

    @PostMapping
    public Result<Complaint> create(@RequestBody Complaint complaint,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(complaintService.create(complaint, userId, userName));
    }

    @PutMapping("/{id}/investigate")
    public Result<Complaint> investigate(@PathVariable Long id,
                                          @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(complaintService.handleInvestigation(id, userId, userName));
    }

    @PutMapping("/{id}/establish")
    public Result<Complaint> establish(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                        @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(complaintService.establishComplaint(id, body.get("handleResult"), userId, userName));
    }

    @PutMapping("/{id}/reject")
    public Result<Complaint> reject(@PathVariable Long id,
                                     @RequestBody Map<String, String> body,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(complaintService.rejectComplaint(id, body.get("handleResult"), userId, userName));
    }

    @PutMapping("/{id}/cancel")
    public Result<Complaint> cancel(@PathVariable Long id,
                                     @RequestBody Map<String, String> body,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(complaintService.cancelComplaint(id, body.get("reason"), userId, userName));
    }

    @PutMapping("/{id}/close")
    public Result<Complaint> close(@PathVariable Long id,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(complaintService.closeComplaint(id, userId, userName));
    }
}
