package com.goldenbridge.app.repository;

import com.goldenbridge.app.config.TestJpaConfig;
import com.goldenbridge.app.entity.SyncHistory;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Import(TestJpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncHistoryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SyncHistoryRepository syncHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        syncHistoryRepository.deleteAll();
        testUser = new User("testuser", "test@example.com", "password");
        userRepository.save(testUser);
    }

    @Test
    void whenSaveSyncHistory_thenSyncHistoryIsSaved() {
        // Given
        SyncHistory syncHistory = new SyncHistory(SyncHistory.SyncType.MANUAL, testUser);

        // When
        SyncHistory savedSyncHistory = syncHistoryRepository.save(syncHistory);

        // Then
        assertThat(savedSyncHistory).isNotNull();
        assertThat(savedSyncHistory.getId()).isNotNull();
        assertThat(savedSyncHistory.getSyncType()).isEqualTo(SyncHistory.SyncType.MANUAL);
        assertThat(savedSyncHistory.getUser()).isEqualTo(testUser);
    }

    @Test
    void whenFindByUserOrderBySyncStartedAtDesc_thenReturnsSyncHistories() {
        // Given
        SyncHistory syncHistory1 = new SyncHistory(SyncHistory.SyncType.MANUAL, testUser);
        SyncHistory syncHistory2 = new SyncHistory(SyncHistory.SyncType.SCHEDULED, testUser);
        syncHistoryRepository.save(syncHistory1);
        syncHistoryRepository.save(syncHistory2);

        // When
        List<SyncHistory> syncHistories = syncHistoryRepository.findByUserOrderBySyncStartedAtDesc(testUser);

        // Then
        assertThat(syncHistories).hasSize(2);
        assertThat(syncHistories.get(0).getSyncStartedAt()).isAfter(syncHistories.get(1).getSyncStartedAt());
    }
}