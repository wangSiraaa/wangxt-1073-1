package com.seafood.scale.controller;

import com.seafood.scale.common.Result;
import com.seafood.scale.entity.ScaleBorrow;
import com.seafood.scale.service.ScaleBorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scale-borrows")
public class ScaleBorrowController {

    @Autowired
    private ScaleBorrowService scaleBorrowService;

    @GetMapping
    public Result<List<ScaleBorrow>> findAll() {
        return Result.success(scaleBorrowService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ScaleBorrow> findById(@PathVariable Long id) {
        return Result.success(scaleBorrowService.findById(id));
    }

    @GetMapping("/scale/{scaleId}")
    public Result<List<ScaleBorrow>> findByScaleId(@PathVariable Long scaleId) {
        return Result.success(scaleBorrowService.findByScaleId(scaleId));
    }

    @GetMapping("/stall/{stallId}")
    public Result<List<ScaleBorrow>> findByStallId(@PathVariable Long stallId) {
        return Result.success(scaleBorrowService.findByBorrowedToStallId(stallId));
    }

    @GetMapping("/active")
    public Result<List<ScaleBorrow>> findActive() {
        return Result.success(scaleBorrowService.findActiveBorrows());
    }

    @PostMapping("/borrow")
    public Result<ScaleBorrow> borrow(@RequestBody Map<String, Object> body,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                       @RequestHeader(value = "X-User-Name", required = false) String userName) {
        Long scaleId = Long.valueOf(body.get("scaleId").toString());
        Long toStallId = body.get("toStallId") != null ? Long.valueOf(body.get("toStallId").toString()) : null;
        Long borrowerId = body.get("borrowerId") != null ? Long.valueOf(body.get("borrowerId").toString()) : null;
        String borrowerName = (String) body.get("borrowerName");
        String borrowReason = (String) body.get("borrowReason");
        LocalDateTime expectedReturn = body.get("expectedReturnAt") != null ?
                LocalDateTime.parse((String) body.get("expectedReturnAt")) : null;
        return Result.success(scaleBorrowService.borrowScale(scaleId, toStallId, borrowerId,
                borrowerName, borrowReason, expectedReturn, userId, userName));
    }

    @PutMapping("/{id}/return")
    public Result<ScaleBorrow> returnScale(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return Result.success(scaleBorrowService.returnScale(id, body.get("returnRemark"), userId, userName));
    }
}
