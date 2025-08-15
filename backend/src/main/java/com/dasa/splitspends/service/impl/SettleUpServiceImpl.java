package com.dasa.splitspends.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.SettleUp;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.SettleUpRepository;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.service.ActivityLogService;
import com.dasa.splitspends.service.NotificationService;
import com.dasa.splitspends.service.SettleUpService;

@Service
@Transactional
public class SettleUpServiceImpl implements SettleUpService {

    private final SettleUpRepository settleUpRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public SettleUpServiceImpl(SettleUpRepository settleUpRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            ActivityLogService activityLogService,
            NotificationService notificationService) {
        this.settleUpRepository = settleUpRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    @Override
    public SettleUp createSettlement(Long groupId, Long payerId, Long payeeId, BigDecimal amount,
            String description, SettleUp.PaymentMethod paymentMethod) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new RuntimeException("Payer not found"));
        User payee = userRepository.findById(payeeId)
                .orElseThrow(() -> new RuntimeException("Payee not found"));

        SettleUp settlement = new SettleUp();
        settlement.setGroup(group);
        settlement.setPayer(payer);
        settlement.setPayee(payee);
        settlement.setAmount(amount);
        settlement.setDescription(description);
        settlement.setPaymentMethod(paymentMethod);
        settlement.setStatus(SettleUp.SettlementStatus.PENDING);
        settlement.setCreatedAt(LocalDateTime.now());

        SettleUp saved = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementCreated(saved, payer);

        // Send notification to payee
        notificationService.sendSettlementNotification(saved);

        return saved;
    }

    @Override
    public SettleUp confirmSettlement(Long settlementId, Long confirmingUserId, String transactionId) {
        SettleUp settlement = settleUpRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));
        User confirmingUser = userRepository.findById(confirmingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!settlement.getPayee().getId().equals(confirmingUserId)) {
            throw new RuntimeException("Only payee can confirm settlement");
        }

        settlement.setStatus(SettleUp.SettlementStatus.CONFIRMED);
        settlement.setTransactionId(transactionId);
        settlement.setConfirmedAt(LocalDateTime.now());
        settlement.setConfirmedBy(confirmingUser);

        SettleUp saved = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementConfirmed(saved, confirmingUser);

        // Send notification to payer
        notificationService.sendSettlementConfirmedNotification(saved);

        return saved;
    }

    @Override
    public SettleUp rejectSettlement(Long settlementId, Long rejectingUserId, String reason) {
        SettleUp settlement = settleUpRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));
        User rejectingUser = userRepository.findById(rejectingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!settlement.getPayee().getId().equals(rejectingUserId)) {
            throw new RuntimeException("Only payee can reject settlement");
        }

        settlement.setStatus(SettleUp.SettlementStatus.REJECTED);
        settlement.setRejectionReason(reason);
        settlement.setRejectedAt(LocalDateTime.now());

        SettleUp saved = settleUpRepository.save(settlement);

        // Log activity
        activityLogService.logSettlementRejected(saved, rejectingUser, reason);

        // Send notification to payer
        notificationService.sendSettlementRejectedNotification(saved);

        return saved;
    }

    @Override
    public List<SettleUp> getPendingSettlements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return settleUpRepository.findPendingByUser(user);
    }

    @Override
    public List<SettleUp> getGroupSettlements(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return settleUpRepository.findByGroupOrderByCreatedAtDesc(group);
    }

    @Override
    public List<SettleUp> getUserSettlementHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return settleUpRepository.findByPayerOrPayeeOrderByCreatedAtDesc(user, user);
    }

    @Override
    public BigDecimal getTotalAmountSettledByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return settleUpRepository.getTotalConfirmedAmountByPayer(user);
    }

    @Override
    public BigDecimal getPendingAmountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return settleUpRepository.getTotalPendingAmountByPayer(user);
    }

    @Override
    public List<SettleUp> getSettlementsBetweenUsers(Long groupId, Long user1Id, Long user2Id) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User1 not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        return settleUpRepository.findBetweenUsersInGroup(group, user1, user2);
    }

    @Override
    public SettleUp getSettlementById(Long settlementId) {
        return settleUpRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));
    }

    @Override
    public void sendSettlementReminders() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(7);
        List<SettleUp> settlements = settleUpRepository.findSettlementsNeedingReminder(reminderThreshold);

        for (SettleUp settlement : settlements) {
            notificationService.sendSettlementReminderNotification(settlement);
        }
    }

    @Override
    public List<SettleUp> getRecentSettlements(Long groupId, int limit) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return settleUpRepository.findRecentByGroup(group, PageRequest.of(0, limit));
    }
}