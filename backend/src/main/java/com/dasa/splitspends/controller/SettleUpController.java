package com.dasa.splitspends.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.SettlementConfirmRequest;
import com.dasa.splitspends.dto.SettlementRejectRequest;
import com.dasa.splitspends.dto.SettlementRequest;
import com.dasa.splitspends.dto.SettlementResponse;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.service.SettleUpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settlements")
public class SettleUpController {

    private final SettleUpService settleUpService;

    public SettleUpController(SettleUpService settleUpService) {
        this.settleUpService = settleUpService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(@Valid @RequestBody SettlementRequest request) {
        SettleUp settlement = settleUpService.createSettlement(
            request.getGroupId(),
            request.getPayerId(),
            request.getPayeeId(),
            request.getAmount(),
            request.getDescription(),
            request.getPaymentMethod()
        );
        return ResponseEntity.ok(SettlementResponse.fromEntity(settlement));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable Long settlementId) {
        SettleUp settlement = settleUpService.getSettlementById(settlementId);
        return ResponseEntity.ok(SettlementResponse.fromEntity(settlement));
    }

    @PutMapping("/{settlementId}/confirm")
    public ResponseEntity<SettlementResponse> confirmSettlement(
            @PathVariable Long settlementId,
            @Valid @RequestBody SettlementConfirmRequest request) {
        SettleUp settlement = settleUpService.confirmSettlement(
            settlementId, 
            request.getConfirmingUserId(),
            request.getTransactionId()
        );
        return ResponseEntity.ok(SettlementResponse.fromEntity(settlement));
    }

    @PutMapping("/{settlementId}/reject")
    public ResponseEntity<SettlementResponse> rejectSettlement(
            @PathVariable Long settlementId,
            @Valid @RequestBody SettlementRejectRequest request) {
        SettleUp settlement = settleUpService.rejectSettlement(
            settlementId,
            request.getRejectingUserId(),
            request.getReason()
        );
        return ResponseEntity.ok(SettlementResponse.fromEntity(settlement));
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<SettlementResponse>> getPendingSettlements(@PathVariable Long userId) {
        List<SettleUp> settlements = settleUpService.getPendingSettlements(userId);
        List<SettlementResponse> response = settlements.stream()
                .map(SettlementResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(@PathVariable Long groupId) {
        List<SettleUp> settlements = settleUpService.getGroupSettlements(groupId);
        List<SettlementResponse> response = settlements.stream()
                .map(SettlementResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<SettlementResponse>> getUserSettlementHistory(@PathVariable Long userId) {
        List<SettleUp> settlements = settleUpService.getUserSettlementHistory(userId);
        List<SettlementResponse> response = settlements.stream()
                .map(SettlementResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/total-settled")
    public ResponseEntity<BigDecimal> getTotalAmountSettledByUser(@PathVariable Long userId) {
        BigDecimal total = settleUpService.getTotalAmountSettledByUser(userId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/user/{userId}/pending-amount")
    public ResponseEntity<BigDecimal> getPendingAmountByUser(@PathVariable Long userId) {
        BigDecimal pending = settleUpService.getPendingAmountByUser(userId);
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/group/{groupId}/between")
    public ResponseEntity<List<SettlementResponse>> getSettlementsBetweenUsers(
            @PathVariable Long groupId,
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        List<SettleUp> settlements = settleUpService.getSettlementsBetweenUsers(groupId, user1Id, user2Id);
        List<SettlementResponse> response = settlements.stream()
                .map(SettlementResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}/recent")
    public ResponseEntity<List<SettlementResponse>> getRecentSettlements(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "10") int limit) {
        List<SettleUp> settlements = settleUpService.getRecentSettlements(groupId, limit);
        List<SettlementResponse> response = settlements.stream()
                .map(SettlementResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-reminders")
    public ResponseEntity<Void> sendSettlementReminders() {
        settleUpService.sendSettlementReminders();
        return ResponseEntity.ok().build();
    }
}