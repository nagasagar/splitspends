package com.dasa.splitspends.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByMembers_Id returns groups for user")
    void testFindByMembers_Id() {
        // Create a valid user with properly initialized collections
        User user = User.builder()
                .email("testuser@example.com")
                .passwordHash("hashedPassword123")
                .name("Test User")
                .groups(new HashSet<>()) // Initialize the collection
                .build();
        user = userRepository.save(user);

        // Create a valid group with admin
        Group group = new Group();
        group.setName("Test Group");
        group.setCreatedBy(user);
        group.setMembers(new HashSet<>()); // Initialize collection
        group.setAdmins(new HashSet<>()); // Initialize collection
        group.getMembers().add(user);
        group.getAdmins().add(user); // Required: Group must have at least one admin
        group = groupRepository.save(group);

        List<Group> groups = groupRepository.findByMembers_Id(user.getId());
        assertThat(groups).isNotEmpty();
        assertThat(groups.get(0).getName()).isEqualTo("Test Group");
    }

    @Test
    @DisplayName("existsByName returns true for existing group name")
    void testExistsByName() {
        // Create a valid user with properly initialized collections
        User user = User.builder()
                .email("admin@example.com")
                .passwordHash("hashedPassword123")
                .name("Admin User")
                .groups(new HashSet<>()) // Initialize the collection
                .build();
        user = userRepository.save(user);

        // Create a valid group with admin
        Group group = new Group();
        group.setName("Unique Group");
        group.setCreatedBy(user);
        group.setMembers(new HashSet<>()); // Initialize collection
        group.setAdmins(new HashSet<>()); // Initialize collection
        group.getAdmins().add(user); // Required: Group must have at least one admin
        group = groupRepository.save(group);

        boolean exists = groupRepository.existsByName("Unique Group");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase returns matching groups")
    void testFindByNameContainingIgnoreCase() {
        // Create a valid user with properly initialized collections
        User user = User.builder()
                .email("groupowner@example.com")
                .passwordHash("hashedPassword123")
                .name("Group Owner")
                .groups(new HashSet<>()) // Initialize the collection
                .build();
        user = userRepository.save(user);

        // Create a valid group with admin
        Group group = new Group();
        group.setName("Alpha Test Group");
        group.setCreatedBy(user);
        group.setMembers(new HashSet<>()); // Initialize collection
        group.setAdmins(new HashSet<>()); // Initialize collection
        group.getAdmins().add(user); // Required: Group must have at least one admin
        groupRepository.save(group);

        List<Group> groups = groupRepository.findByNameContainingIgnoreCase("alpha");
        assertThat(groups).isNotEmpty();
        assertThat(groups.get(0).getName()).containsIgnoringCase("alpha");
    }
}
