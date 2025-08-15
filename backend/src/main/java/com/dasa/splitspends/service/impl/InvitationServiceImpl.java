package com.dasa.splitspends.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.InvitationRepository;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.service.ActivityLogService;
import com.dasa.splitspends.service.GroupService;
import com.dasa.splitspends.service.InvitationService;
import com.dasa.splitspends.service.NotificationService;

@Service
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public InvitationServiceImpl(InvitationRepository invitationRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            GroupService groupService,
            ActivityLogService activityLogService,
            NotificationService notificationService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupService = groupService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    @Override
    public Invitation sendInvitation(Long groupId, String email, Long invitedByUserId, String personalMessage) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User invitedBy = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if invitation already exists
        if (invitationRepository.hasPendingInvitation(email, group)) {
            throw new RuntimeException("Pending invitation already exists for this email");
        }

        // Check if user is already a member
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null && groupService.isUserMemberOfGroup(existingUser.getId(), groupId)) {
            throw new RuntimeException("User is already a member of this group");
        }

        Invitation invitation = new Invitation();
        invitation.setGroup(group);
        invitation.setEmail(email);
        invitation.setInvitedBy(invitedBy);
        invitation.setPersonalMessage(personalMessage);
        invitation.setInvitationToken(UUID.randomUUID().toString());
        invitation.setStatus(Invitation.InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setCreatedAt(LocalDateTime.now());

        Invitation saved = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationSent(saved, invitedBy);

        // Send notification
        notificationService.sendGroupInvitationNotification(saved);

        return saved;
    }

    @Override
    public Invitation acceptInvitation(String token, Long acceptingUserId) {
        Invitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
        User acceptingUser = userRepository.findById(acceptingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new RuntimeException("Invitation has expired");
        }

        // Add user to group
        groupService.addMembers(invitation.getGroup().getId(),
                List.of(acceptingUser.getId()), acceptingUser.getId());

        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitation.setAcceptedBy(acceptingUser);

        Invitation saved = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationAccepted(saved, acceptingUser);

        return saved;
    }

    @Override
    public Invitation declineInvitation(String token, Long decliningUserId, String reason) {
        Invitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
        User decliningUser = userRepository.findById(decliningUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending");
        }

        invitation.setStatus(Invitation.InvitationStatus.DECLINED);
        invitation.setDeclinedAt(LocalDateTime.now());
        invitation.setDeclineReason(reason);

        Invitation saved = invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationDeclined(saved, decliningUser, reason);

        return saved;
    }

    @Override
    public List<Invitation> getGroupInvitations(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return invitationRepository.findByGroupOrderByCreatedAtDesc(group);
    }

    @Override
    public List<Invitation> getUserInvitations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return invitationRepository.findByEmailOrderByCreatedAtDesc(user.getEmail());
    }

    @Override
    public List<Invitation> getPendingInvitations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return invitationRepository.findPendingByEmail(user.getEmail());
    }

    @Override
    public Invitation getInvitationByToken(String token) {
        return invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
    }

    @Override
    public void cancelInvitation(Long invitationId, Long cancelledByUserId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        User cancelledBy = userRepository.findById(cancelledByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending invitations");
        }

        invitation.setStatus(Invitation.InvitationStatus.CANCELLED);
        invitation.setCancelledAt(LocalDateTime.now());

        invitationRepository.save(invitation);

        // Log activity
        activityLogService.logInvitationCancelled(invitation, cancelledBy);
    }

    @Override
    public void expireOldInvitations() {
        int expired = invitationRepository.markExpiredInvitations();
        System.out.println("Expired " + expired + " old invitations");
    }

    @Override
    public void sendInvitationReminders() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(5);
        List<Invitation> invitations = invitationRepository.findInvitationsNeedingReminder(reminderThreshold);

        for (Invitation invitation : invitations) {
            // Send reminder email logic would go here
            invitation.setReminderCount(invitation.getReminderCount() + 1);
            invitation.setLastReminderSentAt(LocalDateTime.now());
            invitationRepository.save(invitation);
        }
    }

    @Override
    public boolean isInvitationValid(String token) {
        return invitationRepository.findByInvitationToken(token)
                .map(invitation -> invitation.getStatus() == Invitation.InvitationStatus.PENDING
                        && invitation.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    public List<Invitation> getInvitationsSentByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return invitationRepository.findByInvitedByOrderByCreatedAtDesc(user);
    }
}