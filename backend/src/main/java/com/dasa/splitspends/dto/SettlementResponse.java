package com.dasa.splitspends.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dasa.splitspends.entity.SettleUp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementResponse {
    private Long id;
    private GroupResponse group;
    private UserResponse payer;
    private UserResponse payee;
    private BigDecimal amount;
    private String description;
    private SettleUp.PaymentMethod paymentMethod;
    private SettleUp.SettlementStatus status;
    private String transactionId;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime rejectedAt;
    private UserResponse confirmedBy;
    
    public static SettlementResponse fromEntity(SettleUp settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .group(settlement.getGroup() != null ? GroupResponse.fromEntity(settlement.getGroup()) : null)
                .payer(settlement.getPayer() != null ? UserResponse.fromEntity(settlement.getPayer()) : null)
                .payee(settlement.getPayee() != null ? UserResponse.fromEntity(settlement.getPayee()) : null)
                .amount(settlement.getAmount())
                .description(settlement.getDescription())
                .paymentMethod(settlement.getPaymentMethod())
                .status(settlement.getStatus())
                .transactionId(settlement.getTransactionId())
                .rejectionReason(settlement.getRejectionReason())
                .createdAt(settlement.getCreatedAt())
                .confirmedAt(settlement.getConfirmedAt())
                .rejectedAt(settlement.getRejectedAt())
                .confirmedBy(settlement.getConfirmedBy() != null ? UserResponse.fromEntity(settlement.getConfirmedBy()) : null)
                .build();
    }
}