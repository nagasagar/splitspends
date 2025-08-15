package com.dasa.splitspends.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.InvitationRequest;
import com.dasa.splitspends.dto.InvitationResponse;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.service.InvitationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<InvitationResponse> sendInvitation(@Valid @RequestBody InvitationRequest request) {
        Invitation invitation = invitationService.sendInvitation(
            request.getGroupId(),
            request.getEmail(),
            request.getInvitedByUserId(),
            request.getPersonalMessage()
        );
        return ResponseEntity.ok(InvitationResponse.fromEntity(invitation));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<InvitationResponse> acceptInvitation(
            @PathVariable String token,
            @RequestParam Long acceptingUserId) {
        Invitation invitation = invitationService.acceptInvitation(token, acceptingUserId);
        return ResponseEntity.ok(InvitationResponse.fromEntity(invitation));
    }

    @PostMapping("/{token}/decline")
    public ResponseEntity<InvitationResponse> declineInvitation(
            @PathVariable String token,
            @RequestParam Long decliningUserId,
            @RequestParam(required = false) String reason) {
        Invitation invitation = invitationService.declineInvitation(token, decliningUserId, reason);
        return ResponseEntity.ok(InvitationResponse.fromEntity(invitation));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<InvitationResponse>> getGroupInvitations(@PathVariable Long groupId) {
        List<Invitation> invitations = invitationService.getGroupInvitations(groupId);
        List<InvitationResponse> response = invitations.stream()
                .map(InvitationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvitationResponse>> getUserInvitations(@PathVariable Long userId) {
        List<Invitation> invitations = invitationService.getUserInvitations(userId);
        List<InvitationResponse> response = invitations.stream()
                .map(InvitationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<InvitationResponse>> getPendingInvitations(@PathVariable Long userId) {
        List<Invitation> invitations = invitationService.getPendingInvitations(userId);
        List<InvitationResponse> response = invitations.stream()
                .map(InvitationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/sent")
    public ResponseEntity<List<InvitationResponse>> getInvitationsSentByUser(@PathVariable Long userId) {
        List<Invitation> invitations = invitationService.getInvitationsSentByUser(userId);
        List<InvitationResponse> response = invitations.stream()
                .map(InvitationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<InvitationResponse> getInvitationByToken(@PathVariable String token) {
        Invitation invitation = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(InvitationResponse.fromEntity(invitation));
    }

    @GetMapping("/token/{token}/valid")
    public ResponseEntity<Boolean> isInvitationValid(@PathVariable String token) {
        boolean valid = invitationService.isInvitationValid(token);
        return ResponseEntity.ok(valid);
    }

    @PostMapping("/{invitationId}/cancel")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long invitationId,
            @RequestParam Long cancelledByUserId) {
        invitationService.cancelInvitation(invitationId, cancelledByUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/expire-old")
    public ResponseEntity<Void> expireOldInvitations() {
        invitationService.expireOldInvitations();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-reminders")
    public ResponseEntity<Void> sendInvitationReminders() {
        invitationService.sendInvitationReminders();
        return ResponseEntity.ok().build();
    }
}