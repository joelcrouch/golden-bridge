package com.goldenbridge.app.repository;

import com.goldenbridge.app.config.TestJpaConfig;
import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.entity.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Import(TestJpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserPreferencesRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userPreferencesRepository.deleteAll();
        testUser = new User("testuser_prefs_test", "test_prefs@example.com", "password");
        userRepository.save(testUser);
    }

    @Test
    void whenSaveUserPreferences_thenUserPreferencesAreSaved() {
        // Given
        UserPreferences preferences = new UserPreferences(testUser);
        preferences.setAutoSyncEnabled(true);

        // When
        UserPreferences savedPreferences = userPreferencesRepository.save(preferences);

        // Then
        assertThat(savedPreferences).isNotNull();
        assertThat(savedPreferences.getId()).isNotNull();
        assertThat(savedPreferences.getAutoSyncEnabled()).isTrue();
        assertThat(savedPreferences.getUser()).isEqualTo(testUser);
    }

    @Test
    void whenFindByUser_thenReturnsUserPreferences() {
        // Given
        UserPreferences preferences = new UserPreferences(testUser);
        userPreferencesRepository.save(preferences);

        // When
        UserPreferences foundPreferences = userPreferencesRepository.findByUser(testUser).orElse(null);

        // Then
        assertThat(foundPreferences).isNotNull();
        assertThat(foundPreferences.getUser()).isEqualTo(testUser);
    }
}