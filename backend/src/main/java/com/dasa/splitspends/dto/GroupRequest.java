package com.dasa.splitspends.dto;

import com.dasa.splitspends.entity.Group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequest {
    
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Created by user ID is required")
    private Long createdByUserId;
    
    private Long updatedByUserId;
    
    private Group.PrivacyLevel privacyLevel = Group.PrivacyLevel.PRIVATE;
    
    @NotBlank(message = "Default currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String defaultCurrency = "USD";
}