package com.dasa.splitspends.dto.group;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.dasa.splitspends.dto.UserResponse;
import com.dasa.splitspends.entity.Group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private UserResponse createdBy;
    private String defaultCurrency;
    private Group.PrivacyLevel privacyLevel;
    private Group.InvitationPolicy invitationPolicy;
    private Boolean allowExternalPayments;
    private Group.GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<UserResponse> members;
    private Set<UserResponse> admins;
    private String groupImageUrl;

    public static GroupResponse fromEntity(Group group) {
        Set<UserResponse> members = group.getMembers() != null ? group.getMembers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toSet()) : null;

        Set<UserResponse> admins = group.getAdmins() != null ? group.getAdmins().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toSet()) : null;

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy() != null ? UserResponse.fromEntity(group.getCreatedBy()) : null)
                .defaultCurrency(group.getDefaultCurrency())
                .privacyLevel(group.getPrivacyLevel())
                .invitationPolicy(group.getInvitationPolicy())
                .allowExternalPayments(group.getAllowExternalPayments())
                .status(group.getStatus())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .members(members)
                .admins(admins)
                .groupImageUrl(group.getGroupImageUrl())
                .build();
    }
}