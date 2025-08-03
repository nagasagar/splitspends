package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.SettleUpRepository;

@Service
@Transactional
public class SettleUpService {

    @Autowired
    private SettleUpRepository settleUpRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Create a new settlement request
     */
    public SettleUp createSettlement(Group group, User payer, User payee, BigDecimal amount,
            User initiatedBy, String notes) {
        validateSettlementCreation(group, payer, payee, amount, initiatedBy);

        SettleUp settlement = SettleUp.builder()
                .group(group)
                .payer(payer)
                .payee(payee)
                .amount(amount)
                .currency(group.getDefaultCurrency())
                .initiatedBy(initiatedBy)
                .notes(notes)
                .status(SettleUp.SettlementStatus.PENDING)
                .build();

        SettleUp saved = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementRequested(initiatedBy, group, saved);

        // Send notification to both payer and payee
        notificationService.createSettlementRequestNotification(payer, initiatedBy, saved, group);
        if (!payer.equals(payee)) {
            notificationService.createSettlementRequestNotification(payee, initiatedBy, saved, group);
        }

        return saved;
    }

    /**
     * Get settlement by ID
     */
    @Transactional(readOnly = true)
    public Optional<SettleUp> getSettlementById(Long id) {
        return settleUpRepository.findById(id);
    }

    /**
     * Update settlement details
     */
    public SettleUp updateSettlement(Long id, BigDecimal amount, String notes,
            SettleUp.PaymentMethod paymentMethod, User updatedBy) {
        SettleUp settlement = getSettlementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        validateSettlementUpdate(settlement, updatedBy);

        settlement.setAmount(amount);
        settlement.setNotes(notes);
        settlement.setPaymentMethod(paymentMethod);

        SettleUp updated = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementUpdated(updatedBy, settlement.getGroup(), updated);

        return updated;
    }

    /**
     * Delete settlement (only if pending)
     */
    public void deleteSettlement(Long id, User deletedBy) {
        SettleUp settlement = getSettlementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        validateSettlementDeletion(settlement, deletedBy);

        settleUpRepository.delete(settlement);

        // Log activity
        activityLogService.logSettlementDeleted(deletedBy, settlement.getGroup(), settlement);
    }

    // ========== SETTLEMENT WORKFLOW OPERATIONS ==========

    /**
     * Confirm settlement completion
     */
    public SettleUp confirmSettlement(Long id, User confirmedBy, String externalTransactionId) {
        SettleUp settlement = getSettlementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        validateSettlementConfirmation(settlement, confirmedBy);

        settlement.confirm(confirmedBy);
        if (externalTransactionId != null) {
            settlement.setExternalTransactionId(externalTransactionId);
        }

        SettleUp confirmed = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementCompleted(confirmedBy, settlement.getGroup(), confirmed);

        // Send notifications
        notificationService.createSettlementCompletedNotification(
                settlement.getPayer(), confirmedBy, confirmed, settlement.getGroup());
        notificationService.createSettlementCompletedNotification(
                settlement.getPayee(), confirmedBy, confirmed, settlement.getGroup());

        return confirmed;
    }

    /**
     * Reject settlement
     */
    public SettleUp rejectSettlement(Long id, User rejectedBy, String reason) {
        SettleUp settlement = getSettlementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        validateSettlementRejection(settlement, rejectedBy);

        settlement.reject(rejectedBy, reason);
        SettleUp rejected = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementRejected(rejectedBy, settlement.getGroup(), rejected);

        // Send notification to initiator
        if (settlement.getInitiatedBy() != null) {
            notificationService.createSettlementRejectedNotification(
                    settlement.getInitiatedBy(), rejectedBy, rejected, settlement.getGroup());
        }

        return rejected;
    }

    /**
     * Mark settlement as in progress
     */
    public SettleUp markInProgress(Long id, User user) {
        SettleUp settlement = getSettlementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        validateSettlementAction(settlement, user);

        settlement.markInProgress();
        SettleUp updated = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementInProgress(user, settlement.getGroup(), updated);

        return updated;
    }

    /**
     * Cancel settlement
     */
    public SettleUp cancelSettlement(Long id, User cancelledBy) {
        SettleUp settlement = getSettlementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        validateSettlementCancellation(settlement, cancelledBy);

        settlement.cancel();
        SettleUp cancelled = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementCancelled(cancelledBy, settlement.getGroup(), cancelled);

        return cancelled;
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Get settlements for a user (as payer or payee)
     */
    @Transactional(readOnly = true)
    public List<SettleUp> getSettlementsForUser(User user) {
        return settleUpRepository.findByUserInvolved(user);
    }

    /**
     * Get pending settlements for a user
     */
    @Transactional(readOnly = true)
    public List<SettleUp> getPendingSettlementsForUser(User user) {
        return settleUpRepository.findPendingByUser(user);
    }

    /**
     * Get settlements for a group
     */
    @Transactional(readOnly = true)
    public Page<SettleUp> getSettlementsForGroup(Group group, Pageable pageable) {
        return settleUpRepository.findByGroupOrderByCreatedAtDesc(group, pageable);
    }

    /**
     * Get settlements between two users in a group
     */
    @Transactional(readOnly = true)
    public List<SettleUp> getSettlementsBetweenUsers(Group group, User user1, User user2) {
        return settleUpRepository.findBetweenUsersInGroup(group, user1, user2);
    }

    /**
     * Get settlement summary for a user in a group
     */
    @Transactional(readOnly = true)
    public List<SettleUp> getUserSettlementSummary(Group group, User user) {
        return settleUpRepository.getUserSettlementSummaryInGroup(group, user);
    }

    // ========== BUSINESS ANALYTICS ==========

    /**
     * Get total amount paid by user
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidByUser(User user) {
        return settleUpRepository.getTotalPaidByUser(user);
    }

    /**
     * Get total amount received by user
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivedByUser(User user) {
        return settleUpRepository.getTotalReceivedByUser(user);
    }

    /**
     * Get pending settlement amount for user
     */
    @Transactional(readOnly = true)
    public BigDecimal getPendingAmountForUser(User user) {
        return settleUpRepository.getPendingAmountByPayer(user);
    }

    /**
     * Get settlement statistics for a group
     */
    @Transactional(readOnly = true)
    public SettlementStats getGroupSettlementStats(Group group) {
        long pendingCount = settleUpRepository.countByGroupAndStatus(group, SettleUp.SettlementStatus.PENDING);
        long completedCount = settleUpRepository.countByGroupAndStatus(group, SettleUp.SettlementStatus.COMPLETED);
        Optional<BigDecimal> averageAmount = settleUpRepository.getAverageSettlementAmountByGroup(group);

        return new SettlementStats(pendingCount, completedCount, averageAmount.orElse(BigDecimal.ZERO));
    }

    // ========== REMINDER AND MAINTENANCE OPERATIONS ==========

    /**
     * Get settlements needing reminders
     */
    @Transactional(readOnly = true)
    public List<SettleUp> getSettlementsNeedingReminder() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(3); // 3 days old
        return settleUpRepository.findSettlementsNeedingReminder(reminderThreshold);
    }

    /**
     * Get expired pending settlements
     */
    @Transactional(readOnly = true)
    public List<SettleUp> getExpiredPendingSettlements() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusDays(30); // 30 days old
        return settleUpRepository.findExpiredPendingSettlements(expirationThreshold);
    }

    /**
     * Process reminder notifications for old settlements
     */
    public void processSettlementReminders() {
        List<SettleUp> settlementsNeedingReminder = getSettlementsNeedingReminder();

        for (SettleUp settlement : settlementsNeedingReminder) {
            // Send reminder notifications
            notificationService.createSettlementReminderNotification(
                    settlement.getPayer(), settlement.getPayee(), settlement, settlement.getGroup());
            notificationService.createSettlementReminderNotification(
                    settlement.getPayee(), settlement.getPayer(), settlement, settlement.getGroup());
        }
    }

    // ========== VALIDATION METHODS ==========

    private void validateSettlementCreation(Group group, User payer, User payee,
            BigDecimal amount, User initiatedBy) {
        if (group == null || !group.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive group");
        }

        if (payer == null || payee == null) {
            throw new IllegalArgumentException("Payer and payee are required");
        }

        if (payer.equals(payee)) {
            throw new IllegalArgumentException("Payer and payee cannot be the same person");
        }

        if (!group.isMember(payer) || !group.isMember(payee)) {
            throw new IllegalArgumentException("Both payer and payee must be group members");
        }

        if (!group.isMember(initiatedBy)) {
            throw new IllegalArgumentException("User must be a group member to create settlements");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be positive");
        }
    }

    private void validateSettlementUpdate(SettleUp settlement, User updatedBy) {
        if (!settlement.isPending()) {
            throw new IllegalStateException("Only pending settlements can be updated");
        }

        if (!settlement.getGroup().isMember(updatedBy)) {
            throw new IllegalArgumentException("User must be a group member to update settlements");
        }

        // Only allow initiator or involved parties to update
        if (!updatedBy.equals(settlement.getInitiatedBy()) &&
                !updatedBy.equals(settlement.getPayer()) &&
                !updatedBy.equals(settlement.getPayee())) {
            throw new IllegalArgumentException("Only settlement participants can update it");
        }
    }

    private void validateSettlementDeletion(SettleUp settlement, User deletedBy) {
        if (!settlement.isPending()) {
            throw new IllegalStateException("Only pending settlements can be deleted");
        }

        // Only allow initiator or group admin to delete
        if (!deletedBy.equals(settlement.getInitiatedBy()) &&
                !settlement.getGroup().isAdmin(deletedBy)) {
            throw new IllegalArgumentException("Only settlement initiator or group admin can delete it");
        }
    }

    private void validateSettlementConfirmation(SettleUp settlement, User confirmedBy) {
        if (!settlement.isPending() && !settlement.getStatus().equals(SettleUp.SettlementStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Only pending or in-progress settlements can be confirmed");
        }

        // Allow payer, payee, or group admin to confirm
        if (!confirmedBy.equals(settlement.getPayer()) &&
                !confirmedBy.equals(settlement.getPayee()) &&
                !settlement.getGroup().isAdmin(confirmedBy)) {
            throw new IllegalArgumentException("Only settlement participants or group admin can confirm it");
        }
    }

    private void validateSettlementRejection(SettleUp settlement, User rejectedBy) {
        if (!settlement.isPending()) {
            throw new IllegalStateException("Only pending settlements can be rejected");
        }

        // Allow payer or payee to reject
        if (!rejectedBy.equals(settlement.getPayer()) && !rejectedBy.equals(settlement.getPayee())) {
            throw new IllegalArgumentException("Only settlement participants can reject it");
        }
    }

    private void validateSettlementAction(SettleUp settlement, User user) {
        if (!settlement.getGroup().isMember(user)) {
            throw new IllegalArgumentException("User must be a group member");
        }
    }

    private void validateSettlementCancellation(SettleUp settlement, User cancelledBy) {
        if (settlement.isCompleted()) {
            throw new IllegalStateException("Completed settlements cannot be cancelled");
        }

        // Allow initiator or group admin to cancel
        if (!cancelledBy.equals(settlement.getInitiatedBy()) &&
                !settlement.getGroup().isAdmin(cancelledBy)) {
            throw new IllegalArgumentException("Only settlement initiator or group admin can cancel it");
        }
    }

    // ========== HELPER CLASSES ==========

    public static class SettlementStats {
        private final long pendingCount;
        private final long completedCount;
        private final BigDecimal averageAmount;

        public SettlementStats(long pendingCount, long completedCount, BigDecimal averageAmount) {
            this.pendingCount = pendingCount;
            this.completedCount = completedCount;
            this.averageAmount = averageAmount;
        }

        public long getPendingCount() {
            return pendingCount;
        }

        public long getCompletedCount() {
            return completedCount;
        }

        public BigDecimal getAverageAmount() {
            return averageAmount;
        }

        public long getTotalCount() {
            return pendingCount + completedCount;
        }
    }
}
