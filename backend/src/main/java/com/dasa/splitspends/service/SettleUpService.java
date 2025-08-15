package com.dasa.splitspends.service;

import java.math.BigDecimal;
import java.util.List;

import com.dasa.splitspends.entity.SettleUp;

public interface SettleUpService {

    /**
     * Create a new settlement between two users.
     */
    SettleUp createSettlement(Long groupId, Long payerId, Long payeeId, BigDecimal amount, 
                            String description, SettleUp.PaymentMethod paymentMethod);

    /**
     * Confirm a pending settlement.
     */
    SettleUp confirmSettlement(Long settlementId, Long confirmingUserId, String transactionId);

    /**
     * Reject a pending settlement.
     */
    SettleUp rejectSettlement(Long settlementId, Long rejectingUserId, String reason);

    /**
     * Get all pending settlements for a user.
     */
    List<SettleUp> getPendingSettlements(Long userId);

    /**
     * Get all settlements for a group.
     */
    List<SettleUp> getGroupSettlements(Long groupId);

    /**
     * Get settlement history for a user.
     */
    List<SettleUp> getUserSettlementHistory(Long userId);

    /**
     * Get total amount settled by a user.
     */
    BigDecimal getTotalAmountSettledByUser(Long userId);

    /**
     * Get pending settlement amount for a user.
     */
    BigDecimal getPendingAmountByUser(Long userId);

    /**
     * Get settlements between two specific users in a group.
     */
    List<SettleUp> getSettlementsBetweenUsers(Long groupId, Long user1Id, Long user2Id);

    /**
     * Get settlement by ID.
     */
    SettleUp getSettlementById(Long settlementId);

    /**
     * Send settlement reminders for overdue settlements.
     */
    void sendSettlementReminders();

    /**
     * Get recent settlements for activity feed.
     */
    List<SettleUp> getRecentSettlements(Long groupId, int limit);
}