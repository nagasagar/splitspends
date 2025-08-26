package com.dasa.splitspends.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.ExpenseSplit;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.ExpenseRepository;
import com.dasa.splitspends.repository.ExpenseSplitRepository;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("qa")
@RequiredArgsConstructor
@Slf4j
public class QaDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Loading QA test data...");

        // Create demo users
        User alice = createUser("Alice QA", "alice.qa@example.com", User.SystemRole.USER);
        User bob = createUser("Bob QA", "bob.qa@example.com", User.SystemRole.USER);
        User charlie = createUser("Charlie QA", "charlie.qa@example.com", User.SystemRole.USER);
        createUser("Admin QA", "admin.qa@example.com", User.SystemRole.ADMIN);
        createUser("Super Admin QA", "superadmin.qa@example.com", User.SystemRole.SUPER_ADMIN);

        log.info("Created {} demo users", 5);

        // Create demo groups
        Group friendsGroup = createGroup("Friends Group", "Group for friends to split expenses", alice,
                Arrays.asList(alice, bob, charlie), Arrays.asList(alice));

        Group workGroup = createGroup("Work Lunch", "Office lunch expenses", bob,
                Arrays.asList(alice, bob), Arrays.asList(bob));

        Group tripGroup = createGroup("Weekend Trip", "Expenses for our weekend getaway", charlie,
                Arrays.asList(alice, bob, charlie), Arrays.asList(charlie, alice));

        log.info("Created {} demo groups", 3);

        // Create demo expenses
        createExpenseWithSplits(friendsGroup, alice, "Dinner at Restaurant",
                new BigDecimal("120.00"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(alice, bob, charlie));

        createExpenseWithSplits(workGroup, bob, "Office Lunch",
                new BigDecimal("45.50"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(alice, bob));

        createExpenseWithSplits(tripGroup, charlie, "Hotel Booking",
                new BigDecimal("300.00"), Expense.ExpenseCategory.ACCOMMODATION,
                Arrays.asList(alice, bob, charlie));

        createExpenseWithSplits(tripGroup, alice, "Gas for Trip",
                new BigDecimal("80.25"), Expense.ExpenseCategory.TRANSPORTATION,
                Arrays.asList(alice, bob, charlie));

        createExpenseWithSplits(friendsGroup, bob, "Movie Tickets",
                new BigDecimal("36.00"), Expense.ExpenseCategory.ENTERTAINMENT,
                Arrays.asList(alice, bob, charlie));

        log.info("Created {} demo expenses with splits", 5);
        log.info("QA test data loading completed successfully!");
        log.info("You can now test with the following users:");
        log.info("- alice.qa@example.com (User)");
        log.info("- bob.qa@example.com (User)");
        log.info("- charlie.qa@example.com (User)");
        log.info("- admin.qa@example.com (Admin)");
        log.info("- superadmin.qa@example.com (Super Admin)");
        log.info("All users have password: 'password123'");
    }

    private User createUser(String name, String email, User.SystemRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setSystemRole(role);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPreferredCurrency("USD");
        user.setJoinedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEmailNotifications(true);
        user.setPushNotifications(true);
        user.setPaymentReminders(true);
        return userRepository.save(user);
    }

    private Group createGroup(String name, String description, User creator,
            List<User> members, List<User> admins) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setCreatedBy(creator);
        group.setStatus(Group.GroupStatus.ACTIVE);
        group.setPrivacyLevel(Group.PrivacyLevel.PRIVATE);
        group.setInvitationPolicy(Group.InvitationPolicy.ADMIN_ONLY);
        group.setDefaultCurrency("USD");
        group.setAllowExternalPayments(true);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        group = groupRepository.save(group);

        // Add members and admins using JPA relationships
        group.setMembers(new HashSet<>(members));
        group.setAdmins(new HashSet<>(admins));
        group = groupRepository.save(group);

        return group;
    }

    private void createExpenseWithSplits(Group group, User paidBy, String description,
            BigDecimal amount, Expense.ExpenseCategory category,
            List<User> splitBetween) {
        // Create expense
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(LocalDateTime.now().minusDays((long) (Math.random() * 30))); // Random date within last 30 days
        expense.setCategory(category);
        expense.setCurrency("USD");
        expense.setStatus(Expense.ExpenseStatus.CONFIRMED);
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(paidBy);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        expense = expenseRepository.save(expense);

        // Create equal splits
        BigDecimal splitAmount = amount.divide(new BigDecimal(splitBetween.size()), 2, RoundingMode.HALF_UP);

        for (User user : splitBetween) {
            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(expense);
            split.setUser(user);
            split.setSplitType(ExpenseSplit.SplitType.EQUAL);
            split.setShareAmount(splitAmount);
            split.setSettled(Math.random() > 0.7); // 30% chance of being settled
            split.setCreatedAt(LocalDateTime.now());
            split.setUpdatedAt(LocalDateTime.now());

            if (split.isSettled()) {
                split.setSettledAt(LocalDateTime.now());
                split.setSettledBy(user);
            }

            expenseSplitRepository.save(split);
        }
    }
}
