package com.dasa.splitspends.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementConfirmRequest {
    
    @NotNull(message = "Confirming user ID is required")
    private Long confirmingUserId;
    
    private String transactionId;
}