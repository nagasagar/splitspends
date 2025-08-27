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
        log.info("Loading comprehensive QA test data...");

        // Create demo users with Indian names and context
        User alice = createUser("Alice Sharma", "alice.sharma@example.com", User.SystemRole.USER);
        User bob = createUser("Bob Patel", "bob.patel@example.com", User.SystemRole.USER);
        User charlie = createUser("Charlie Kumar", "charlie.kumar@example.com", User.SystemRole.USER);
        User diana = createUser("Diana Singh", "diana.singh@example.com", User.SystemRole.USER);
        User eve = createUser("Eve Gupta", "eve.gupta@example.com", User.SystemRole.USER);
        User frank = createUser("Frank Reddy", "frank.reddy@example.com", User.SystemRole.USER);
        createUser("Admin User", "admin.qa@example.com", User.SystemRole.ADMIN);
        createUser("Super Admin", "superadmin.qa@example.com", User.SystemRole.SUPER_ADMIN);

        log.info("Created {} demo users", 8);

        // Create diverse demo groups to showcase different scenarios
        Group goaTrip = createGroup("Goa Trip 2025", "Beach vacation with college friends", alice,
                Arrays.asList(alice, bob, charlie, diana), Arrays.asList(alice, bob));

        Group officeLunch = createGroup("Office Lunch Squad", "Daily lunch expenses for office team", bob,
                Arrays.asList(alice, bob, charlie, eve), Arrays.asList(bob));

        Group roommates = createGroup("Flat 3B Roommates", "Shared apartment expenses", charlie,
                Arrays.asList(charlie, diana, eve), Arrays.asList(charlie, diana));

        Group familyExpenses = createGroup("Family Expenses", "Monthly family shared costs", diana,
                Arrays.asList(diana, eve, frank), Arrays.asList(diana));

        Group weekendHangout = createGroup("Weekend Squad", "Friends weekend activities", eve,
                Arrays.asList(alice, bob, eve, frank), Arrays.asList(eve));

        Group gymBuddies = createGroup("Gym Membership", "Shared gym and fitness expenses", frank,
                Arrays.asList(alice, frank), Arrays.asList(frank));

        log.info("Created {} demo groups", 6);

        // Create comprehensive expenses covering all categories and split types

        // === GOA TRIP EXPENSES ===
        createExpenseWithEqualSplits(goaTrip, alice, "Flight Tickets to Goa",
                new BigDecimal("24000.00"), Expense.ExpenseCategory.TRANSPORTATION,
                Arrays.asList(alice, bob, charlie, diana));

        createExpenseWithCustomSplits(goaTrip, bob, "Beach Resort Booking",
                new BigDecimal("18000.00"), Expense.ExpenseCategory.ACCOMMODATION,
                Arrays.asList(
                        new SplitInfo(alice, new BigDecimal("6000.00")),
                        new SplitInfo(bob, new BigDecimal("6000.00")),
                        new SplitInfo(charlie, new BigDecimal("4000.00")),
                        new SplitInfo(diana, new BigDecimal("2000.00"))));

        createExpenseWithPercentageSplits(goaTrip, charlie, "Scuba Diving Activity",
                new BigDecimal("8000.00"), Expense.ExpenseCategory.ENTERTAINMENT,
                Arrays.asList(
                        new SplitInfo(alice, new BigDecimal("30.0")), // 30%
                        new SplitInfo(bob, new BigDecimal("30.0")), // 30%
                        new SplitInfo(charlie, new BigDecimal("25.0")), // 25%
                        new SplitInfo(diana, new BigDecimal("15.0")) // 15%
                ));

        createExpenseWithEqualSplits(goaTrip, diana, "Seafood Dinner at Beach Shack",
                new BigDecimal("3200.00"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(alice, bob, charlie, diana));

        createExpenseWithEqualSplits(goaTrip, alice, "Taxi and Auto Rickshaw",
                new BigDecimal("1800.00"), Expense.ExpenseCategory.TRANSPORTATION,
                Arrays.asList(alice, bob, charlie));

        // === OFFICE LUNCH EXPENSES ===
        createExpenseWithEqualSplits(officeLunch, bob, "Pizza Lunch Order",
                new BigDecimal("1600.00"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(alice, bob, charlie, eve));

        createExpenseWithCustomSplits(officeLunch, alice, "Birthday Cake for Colleague",
                new BigDecimal("2400.00"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(
                        new SplitInfo(alice, new BigDecimal("800.00")),
                        new SplitInfo(bob, new BigDecimal("600.00")),
                        new SplitInfo(charlie, new BigDecimal("600.00")),
                        new SplitInfo(eve, new BigDecimal("400.00"))));

        createExpenseWithEqualSplits(officeLunch, charlie, "Coffee Shop Visit",
                new BigDecimal("800.00"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(alice, bob, charlie, eve));

        // === ROOMMATES EXPENSES ===
        createExpenseWithPercentageSplits(roommates, charlie, "Monthly Electricity Bill",
                new BigDecimal("4500.00"), Expense.ExpenseCategory.UTILITIES,
                Arrays.asList(
                        new SplitInfo(charlie, new BigDecimal("40.0")), // Master bedroom
                        new SplitInfo(diana, new BigDecimal("35.0")), // Regular room
                        new SplitInfo(eve, new BigDecimal("25.0")) // Shared room
                ));

        createExpenseWithEqualSplits(roommates, diana, "Grocery Shopping",
                new BigDecimal("3600.00"), Expense.ExpenseCategory.GROCERIES,
                Arrays.asList(charlie, diana, eve));

        createExpenseWithCustomSplits(roommates, eve, "WiFi Internet Bill",
                new BigDecimal("1200.00"), Expense.ExpenseCategory.UTILITIES,
                Arrays.asList(
                        new SplitInfo(charlie, new BigDecimal("500.00")),
                        new SplitInfo(diana, new BigDecimal("400.00")),
                        new SplitInfo(eve, new BigDecimal("300.00"))));

        createExpenseWithEqualSplits(roommates, charlie, "House Cleaning Service",
                new BigDecimal("2000.00"), Expense.ExpenseCategory.OTHER,
                Arrays.asList(charlie, diana, eve));

        // === FAMILY EXPENSES ===
        createExpenseWithPercentageSplits(familyExpenses, diana, "Monthly Grocery Shopping",
                new BigDecimal("12000.00"), Expense.ExpenseCategory.GROCERIES,
                Arrays.asList(
                        new SplitInfo(diana, new BigDecimal("50.0")), // Main earner
                        new SplitInfo(eve, new BigDecimal("30.0")), // Contributing member
                        new SplitInfo(frank, new BigDecimal("20.0")) // Student
                ));

        createExpenseWithEqualSplits(familyExpenses, eve, "Medical Insurance Premium",
                new BigDecimal("15000.00"), Expense.ExpenseCategory.HEALTHCARE,
                Arrays.asList(diana, eve, frank));

        createExpenseWithCustomSplits(familyExpenses, frank, "Festival Shopping",
                new BigDecimal("8000.00"), Expense.ExpenseCategory.SHOPPING,
                Arrays.asList(
                        new SplitInfo(diana, new BigDecimal("4000.00")),
                        new SplitInfo(eve, new BigDecimal("2500.00")),
                        new SplitInfo(frank, new BigDecimal("1500.00"))));

        // === WEEKEND HANGOUT EXPENSES ===
        createExpenseWithEqualSplits(weekendHangout, eve, "Movie Tickets - Latest Bollywood Film",
                new BigDecimal("1200.00"), Expense.ExpenseCategory.ENTERTAINMENT,
                Arrays.asList(alice, bob, eve, frank));

        createExpenseWithPercentageSplits(weekendHangout, alice, "Bowling and Gaming Zone",
                new BigDecimal("2800.00"), Expense.ExpenseCategory.ENTERTAINMENT,
                Arrays.asList(
                        new SplitInfo(alice, new BigDecimal("25.0")),
                        new SplitInfo(bob, new BigDecimal("25.0")),
                        new SplitInfo(eve, new BigDecimal("30.0")),
                        new SplitInfo(frank, new BigDecimal("20.0"))));

        createExpenseWithEqualSplits(weekendHangout, bob, "Dinner at Mall Food Court",
                new BigDecimal("1600.00"), Expense.ExpenseCategory.FOOD_DRINKS,
                Arrays.asList(alice, bob, eve, frank));

        // === GYM EXPENSES ===
        createExpenseWithCustomSplits(gymBuddies, frank, "Annual Gym Membership",
                new BigDecimal("24000.00"), Expense.ExpenseCategory.OTHER,
                Arrays.asList(
                        new SplitInfo(alice, new BigDecimal("12000.00")), // Full year
                        new SplitInfo(frank, new BigDecimal("12000.00")) // Full year
                ));

        createExpenseWithEqualSplits(gymBuddies, alice, "Protein Supplements",
                new BigDecimal("3000.00"), Expense.ExpenseCategory.OTHER,
                Arrays.asList(alice, frank));

        // === MISCELLANEOUS EXPENSES FOR VARIETY ===
        createExpenseWithEqualSplits(goaTrip, bob, "Souvenir Shopping",
                new BigDecimal("2000.00"), Expense.ExpenseCategory.SHOPPING,
                Arrays.asList(alice, bob, charlie, diana));

        createExpenseWithCustomSplits(officeLunch, eve, "Team Building Event",
                new BigDecimal("5000.00"), Expense.ExpenseCategory.ENTERTAINMENT,
                Arrays.asList(
                        new SplitInfo(alice, new BigDecimal("1200.00")),
                        new SplitInfo(bob, new BigDecimal("1300.00")),
                        new SplitInfo(charlie, new BigDecimal("1200.00")),
                        new SplitInfo(eve, new BigDecimal("1300.00"))));

        log.info("Created {} demo expenses with various split types", 22);
        log.info("QA test data loading completed successfully!");
        log.info("=== TEST USERS (Password: 'password123') ===");
        log.info("- alice.sharma@example.com (User) - Active in multiple groups");
        log.info("- bob.patel@example.com (User) - Group admin, office lunch organizer");
        log.info("- charlie.kumar@example.com (User) - Roommate, handles utilities");
        log.info("- diana.singh@example.com (User) - Family expenses manager");
        log.info("- eve.gupta@example.com (User) - Weekend activities organizer");
        log.info("- frank.reddy@example.com (User) - Gym buddy, student");
        log.info("- admin.qa@example.com (Admin)");
        log.info("- superadmin.qa@example.com (Super Admin)");
        log.info("=== GROUPS CREATED ===");
        log.info("- Goa Trip 2025: Vacation expenses with custom/percentage splits");
        log.info("- Office Lunch Squad: Daily office meal expenses");
        log.info("- Flat 3B Roommates: Shared apartment costs");
        log.info("- Family Expenses: Household and family costs");
        log.info("- Weekend Squad: Entertainment and social activities");
        log.info("- Gym Membership: Fitness and health expenses");
        log.info("All expenses use INR currency and demonstrate equal, custom, and percentage splits!");
    }

    private User createUser(String name, String email, User.SystemRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setSystemRole(role);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPreferredCurrency("INR"); // Default to Indian Rupees
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
        group.setDefaultCurrency("INR"); // Default to Indian Rupees
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
        expense.setCurrency("INR"); // Use Indian Rupees
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

    // Helper method for equal splits
    private void createExpenseWithEqualSplits(Group group, User paidBy, String description,
            BigDecimal amount, Expense.ExpenseCategory category,
            List<User> splitBetween) {
        createExpenseWithSplits(group, paidBy, description, amount, category, splitBetween);
    }

    // Helper method for custom amount splits
    private void createExpenseWithCustomSplits(Group group, User paidBy, String description,
            BigDecimal amount, Expense.ExpenseCategory category,
            List<SplitInfo> customSplits) {

        // Create expense
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
        expense.setCategory(category);
        expense.setCurrency("INR");
        expense.setStatus(Expense.ExpenseStatus.CONFIRMED);
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(paidBy);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        expense = expenseRepository.save(expense);

        // Create custom splits
        for (SplitInfo splitInfo : customSplits) {
            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(expense);
            split.setUser(splitInfo.getUser());
            split.setSplitType(ExpenseSplit.SplitType.EXACT_AMOUNT);
            split.setShareAmount(splitInfo.getAmount());
            split.setSettled(Math.random() > 0.7); // 30% chance of being settled
            split.setCreatedAt(LocalDateTime.now());
            split.setUpdatedAt(LocalDateTime.now());

            if (split.isSettled()) {
                split.setSettledAt(LocalDateTime.now());
                split.setSettledBy(splitInfo.getUser());
            }

            expenseSplitRepository.save(split);
        }
    }

    // Helper method for percentage splits
    private void createExpenseWithPercentageSplits(Group group, User paidBy, String description,
            BigDecimal amount, Expense.ExpenseCategory category,
            List<SplitInfo> percentageSplits) {

        // Create expense
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
        expense.setCategory(category);
        expense.setCurrency("INR");
        expense.setStatus(Expense.ExpenseStatus.CONFIRMED);
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(paidBy);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        expense = expenseRepository.save(expense);

        // Create percentage-based splits
        for (SplitInfo splitInfo : percentageSplits) {
            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(expense);
            split.setUser(splitInfo.getUser());
            split.setSplitType(ExpenseSplit.SplitType.PERCENTAGE);

            // Calculate amount from percentage
            BigDecimal shareAmount = amount.multiply(splitInfo.getAmount())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            split.setShareAmount(shareAmount);

            split.setSettled(Math.random() > 0.7); // 30% chance of being settled
            split.setCreatedAt(LocalDateTime.now());
            split.setUpdatedAt(LocalDateTime.now());

            if (split.isSettled()) {
                split.setSettledAt(LocalDateTime.now());
                split.setSettledBy(splitInfo.getUser());
            }

            expenseSplitRepository.save(split);
        }
    }

    // Inner class to hold split information
    public static class SplitInfo {
        private final User user;
        private final BigDecimal amount; // This can be amount or percentage based on context

        public SplitInfo(User user, BigDecimal amount) {
            this.user = user;
            this.amount = amount;
        }

        public User getUser() {
            return user;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}
