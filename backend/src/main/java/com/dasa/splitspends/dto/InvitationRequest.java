package com.dasa.splitspends.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InvitationRequest {
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotNull(message = "Invited by user ID is required")
    private Long invitedByUserId;
    
    @Size(max = 500, message = "Personal message cannot exceed 500 characters")
    private String personalMessage;
}