package com.dasa.splitspends.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dasa.splitspends.repository.ExpenseRepository;
import com.dasa.splitspends.repository.GroupRepository;
import com.dasa.splitspends.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("qa")
class QaDataLoaderTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    void testQaDataLoaded() {
        // Verify users are created
        assertEquals(5, userRepository.count(), "Should have 5 QA users");

        // Verify groups are created
        assertEquals(3, groupRepository.count(), "Should have 3 QA groups");

        // Verify expenses are created
        assertEquals(5, expenseRepository.count(), "Should have 5 QA expenses");

        // Verify specific user exists
        assertTrue(userRepository.findByEmail("alice.qa@example.com").isPresent(),
                "Alice QA user should exist");
    }
}
