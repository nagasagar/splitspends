package com.dasa.splitspends.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.dasa.splitspends.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by email")
    void testSaveAndFindByEmail() {
        User user = new User();
        user.setName("RepoTest");
        user.setEmail("repotest@example.com");
        user.setPasswordHash("hashedpassword");
        user.setEmailVerified(false);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("repotest@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("RepoTest");
    }

    @Test
    @DisplayName("Should return empty for non-existent email")
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("doesnotexist@example.com");
        assertThat(found).isNotPresent();
    }
}
