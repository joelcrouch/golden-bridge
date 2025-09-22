package com.goldenbridge.app.repository;

import com.goldenbridge.app.config.TestJpaConfig;
import com.goldenbridge.app.entity.GarminCredentials;
import com.goldenbridge.app.entity.User;
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
class GarminCredentialsRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private GarminCredentialsRepository garminCredentialsRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        garminCredentialsRepository.deleteAll();
        testUser = new User("testuser_garmin_test", "test_garmin@example.com", "password");
        userRepository.save(testUser);
    }

    @Test
    void whenSaveCredentials_thenCredentialsAreSaved() {
        // Given
        GarminCredentials credentials = new GarminCredentials("garmin_user", "encrypted_password", testUser);

        // When
        GarminCredentials savedCredentials = garminCredentialsRepository.save(credentials);

        // Then
        assertThat(savedCredentials).isNotNull();
        assertThat(savedCredentials.getId()).isNotNull();
        assertThat(savedCredentials.getGarminUsername()).isEqualTo("garmin_user");
        assertThat(savedCredentials.getUser()).isEqualTo(testUser);
    }

    @Test
    void whenFindByUser_thenReturnsCredentials() {
        // Given
        GarminCredentials credentials = new GarminCredentials("garmin_user", "encrypted_password", testUser);
        garminCredentialsRepository.save(credentials);

        // When
        GarminCredentials foundCredentials = garminCredentialsRepository.findByUser(testUser).orElse(null);

        // Then
        assertThat(foundCredentials).isNotNull();
        assertThat(foundCredentials.getGarminUsername()).isEqualTo("garmin_user");
    }
}