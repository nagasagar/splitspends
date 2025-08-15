package com.dasa.splitspends.dto;

import java.math.BigDecimal;

import com.dasa.splitspends.entity.SettleUp;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SettlementRequest {
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @NotNull(message = "Payer ID is required")
    private Long payerId;
    
    @NotNull(message = "Payee ID is required")
    private Long payeeId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotNull(message = "Payment method is required")
    private SettleUp.PaymentMethod paymentMethod;
}