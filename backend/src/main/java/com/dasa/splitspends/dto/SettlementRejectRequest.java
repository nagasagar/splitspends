package com.dasa.splitspends.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SettlementRejectRequest {
    
    @NotNull(message = "Rejecting user ID is required")
    private Long rejectingUserId;
    
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;
}