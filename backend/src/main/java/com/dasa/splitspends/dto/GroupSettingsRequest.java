package com.dasa.splitspends.dto;

import java.math.BigDecimal;

import com.dasa.splitspends.entity.Group;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupSettingsRequest {
    
    @NotNull(message = "Updated by user ID is required")
    private Long updatedByUserId;
    
    private Group.InvitationPolicy invitationPolicy;
    
    private BigDecimal autoSettleThreshold;
    
    private Boolean allowExternalPayments;
}