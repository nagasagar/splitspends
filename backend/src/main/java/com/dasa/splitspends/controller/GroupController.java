package com.dasa.splitspends.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.dto.GroupRequest;
import com.dasa.splitspends.dto.GroupResponse;
import com.dasa.splitspends.dto.GroupSettingsRequest;
import com.dasa.splitspends.dto.UserResponse;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.service.GroupService;
import com.dasa.splitspends.service.impl.GroupServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        Group group = groupService.createGroup(
            request.getName(),
            request.getDescription(),
            request.getCreatedByUserId(),
            request.getPrivacyLevel(),
            request.getDefaultCurrency()
        );
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupRequest request) {
        Group group = groupService.updateGroup(
            groupId,
            request.getName(),
            request.getDescription(),
            request.getPrivacyLevel(),
            request.getDefaultCurrency(),
            request.getUpdatedByUserId()
        );
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId,
            @RequestParam Long deletedByUserId) {
        groupService.deleteGroup(groupId, deletedByUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMembers(
            @PathVariable Long groupId,
            @RequestParam List<Long> userIds,
            @RequestParam Long addedByUserId) {
        Group group = groupService.addMembers(groupId, userIds, addedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam Long removedByUserId) {
        Group group = groupService.removeMember(groupId, userId, removedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @PutMapping("/{groupId}/members/{userId}/promote")
    public ResponseEntity<GroupResponse> promoteToAdmin(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam Long promotedByUserId) {
        Group group = groupService.promoteToAdmin(groupId, userId, promotedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @PutMapping("/{groupId}/members/{userId}/demote")
    public ResponseEntity<GroupResponse> demoteAdmin(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam Long demotedByUserId) {
        Group group = groupService.demoteAdmin(groupId, userId, demotedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable Long userId) {
        List<Group> groups = groupService.getUserGroups(userId);
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/created")
    public ResponseEntity<List<GroupResponse>> getGroupsCreatedByUser(@PathVariable Long userId) {
        List<Group> groups = groupService.getGroupsCreatedByUser(userId);
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GroupResponse>> searchPublicGroups(@RequestParam String query) {
        List<Group> groups = groupService.searchPublicGroups(query);
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserResponse>> getGroupMembers(@PathVariable Long groupId) {
        Set<User> members = groupService.getGroupMembers(groupId);
        List<UserResponse> response = members.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/admins")
    public ResponseEntity<List<UserResponse>> getGroupAdmins(@PathVariable Long groupId) {
        Set<User> admins = groupService.getGroupAdmins(groupId);
        List<UserResponse> response = admins.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{groupId}/archive")
    public ResponseEntity<GroupResponse> archiveGroup(
            @PathVariable Long groupId,
            @RequestParam Long archivedByUserId) {
        Group group = groupService.archiveGroup(groupId, archivedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @PutMapping("/{groupId}/reactivate")
    public ResponseEntity<GroupResponse> reactivateGroup(
            @PathVariable Long groupId,
            @RequestParam Long reactivatedByUserId) {
        Group group = groupService.reactivateGroup(groupId, reactivatedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @PutMapping("/{groupId}/settings")
    public ResponseEntity<GroupResponse> updateGroupSettings(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupSettingsRequest request) {
        Group group = groupService.updateGroupSettings(
            groupId,
            request.getInvitationPolicy(),
            request.getAutoSettleThreshold(),
            request.getAllowExternalPayments(),
            request.getUpdatedByUserId()
        );
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @GetMapping("/{groupId}/stats")
    public ResponseEntity<GroupServiceImpl.GroupStats> getGroupStats(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupStats(groupId));
    }

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<List<GroupServiceImpl.UserBalance>> getGroupBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupBalances(groupId));
    }

    @PostMapping(value = "/{groupId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GroupResponse> uploadGroupImage(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long uploadedByUserId) {
        Group group = groupService.uploadGroupImage(groupId, file, uploadedByUserId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @GetMapping("/{groupId}/invitation-candidates")
    public ResponseEntity<List<UserResponse>> getUsersForInvitation(
            @PathVariable Long groupId,
            @RequestParam String searchQuery) {
        List<User> users = groupService.getUsersForInvitation(groupId, searchQuery);
        List<UserResponse> response = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
}