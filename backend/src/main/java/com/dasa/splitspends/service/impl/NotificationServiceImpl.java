package com.dasa.splitspends.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.Invitation;
import com.dasa.splitspends.entity.Notification;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.NotificationRepository;
import com.dasa.splitspends.service.NotificationService;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(User recipient, String title, String message, 
                                         Notification.NotificationType type, Notification.Priority priority) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationRepository.findUnreadByUserId(userId);
        }
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<Notification> getUserNotificationsPaginated(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    @Override
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadForUser(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldNotifications(threshold);
    }

    @Override
    public void sendExpenseNotification(Expense expense) {
        String title = "New Expense Added";
        String message = String.format("%s added expense: %s ($%.2f)", 
            expense.getPaidBy().getName(), expense.getDescription(), expense.getAmount());
        
        // Send to all group members except the one who created the expense
        expense.getGroup().getMembers().forEach(member -> {
            if (!member.getId().equals(expense.getPaidBy().getId())) {
                createNotification(member, title, message, 
                    Notification.NotificationType.EXPENSE_ADDED, Notification.Priority.NORMAL);
            }
        });
    }

    @Override
    public void sendExpenseUpdatedNotification(Expense expense, User updatedBy) {
        String title = "Expense Updated";
        String message = String.format("%s updated expense: %s", 
            updatedBy.getName(), expense.getDescription());
        
        expense.getGroup().getMembers().forEach(member -> {
            if (!member.getId().equals(updatedBy.getId())) {
                createNotification(member, title, message, 
                    Notification.NotificationType.EXPENSE_UPDATED, Notification.Priority.NORMAL);
            }
        });
    }

    @Override
    public void sendSettlementNotification(SettleUp settlement) {
        String title = "Settlement Request";
        String message = String.format("%s wants to settle $%.2f with you", 
            settlement.getPayer().getName(), settlement.getAmount());
        
        createNotification(settlement.getPayee(), title, message, 
            Notification.NotificationType.SETTLEMENT_REQUESTED, Notification.Priority.HIGH);
    }

    @Override
    public void sendSettlementConfirmedNotification(SettleUp settlement) {
        String title = "Settlement Confirmed";
        String message = String.format("%s confirmed your settlement of $%.2f", 
            settlement.getConfirmedBy().getName(), settlement.getAmount());
        
        createNotification(settlement.getPayer(), title, message, 
            Notification.NotificationType.SETTLEMENT_CONFIRMED, Notification.Priority.NORMAL);
    }

    @Override
    public void sendSettlementRejectedNotification(SettleUp settlement) {
        String title = "Settlement Rejected";
        String message = String.format("%s rejected your settlement of $%.2f", 
            settlement.getPayee().getName(), settlement.getAmount());
        
        createNotification(settlement.getPayer(), title, message, 
            Notification.NotificationType.SETTLEMENT_REJECTED, Notification.Priority.HIGH);
    }

    @Override
    public void sendSettlementReminderNotification(SettleUp settlement) {
        String title = "Settlement Reminder";
        String message = String.format("Reminder: %s wants to settle $%.2f with you", 
            settlement.getPayer().getName(), settlement.getAmount());
        
        createNotification(settlement.getPayee(), title, message, 
            Notification.NotificationType.SETTLEMENT_REMINDER, Notification.Priority.NORMAL);
    }

    @Override
    public void sendGroupInvitationNotification(Invitation invitation) {
        String title = "Group Invitation";
        String message = String.format("%s invited you to join group: %s", 
            invitation.getInvitedBy().getName(), invitation.getGroup().getName());
        
        // For invitations, we might send to email if user doesn't exist yet
        // For now, assuming user exists
        if (invitation.getInvitedUser() != null) {
            createNotification(invitation.getInvitedUser(), title, message, 
                Notification.NotificationType.GROUP_INVITATION, Notification.Priority.HIGH);
        }
    }

    @Override
    public void sendGroupMemberAddedNotification(Group group, User newMember, User addedBy) {
        String title = "New Group Member";
        String message = String.format("%s added %s to group: %s", 
            addedBy.getName(), newMember.getName(), group.getName());
        
        group.getMembers().forEach(member -> {
            if (!member.getId().equals(addedBy.getId()) && !member.getId().equals(newMember.getId())) {
                createNotification(member, title, message, 
                    Notification.NotificationType.GROUP_MEMBER_ADDED, Notification.Priority.NORMAL);
            }
        });
    }

    @Override
    public List<Notification> getActivityFeedNotifications(Long userId, LocalDateTime since) {
        return notificationRepository.findActivityFeedNotifications(userId, since);
    }

    @Override
    public List<Notification> getHighPriorityNotifications(Long userId) {
        return notificationRepository.findHighPriorityByUserId(userId);
    }
}