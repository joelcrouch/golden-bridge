package com.goldenbridge.app.repository;

import com.goldenbridge.app.config.TestJpaConfig;
import com.goldenbridge.app.entity.Activity;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Import(TestJpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActivityRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        activityRepository.deleteAll();
        testUser = new User("testuser", "test@example.com", "password");
        userRepository.save(testUser);
    }

    @Test
    void whenSaveActivity_thenActivityIsSaved() {
        // Given
        Activity activity = new Activity("garmin123", "Morning Run", LocalDateTime.now(), testUser);

        // When
        Activity savedActivity = activityRepository.save(activity);

        // Then
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.getId()).isNotNull();
        assertThat(savedActivity.getGarminActivityId()).isEqualTo("garmin123");
        assertThat(savedActivity.getUser()).isEqualTo(testUser);
    }

    @Test
    void whenFindByUser_thenReturnsActivities() {
        // Given
        Activity activity1 = new Activity("garmin123", "Morning Run", LocalDateTime.now(), testUser);
        Activity activity2 = new Activity("garmin456", "Evening Walk", LocalDateTime.now(), testUser);
        activityRepository.save(activity1);
        activityRepository.save(activity2);

        // When
        List<Activity> activities = activityRepository.findByUser(testUser);

        // Then
        assertThat(activities).hasSize(2);
    }
}