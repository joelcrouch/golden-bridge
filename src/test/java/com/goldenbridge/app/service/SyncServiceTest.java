package com.goldenbridge.app.service;

import com.goldenbridge.app.entity.Activity;
import com.goldenbridge.app.entity.SyncHistory;
import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.repository.ActivityRepository;
import com.goldenbridge.app.repository.SyncHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private SyncHistoryRepository syncHistoryRepository;

    @InjectMocks
    private SyncService syncService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedpassword");
        testUser.setId(1L);
    }

    @Test
    void testSyncActivities_newActivities() {
        // Given
        String activitiesJson = "[{\"activityId\": \"1\", \"activityName\": \"Run 1\", \"startTimeGMT\": \"2025-10-01 10:00:00\"},"
                                 + "{\"activityId\": \"2\", \"activityName\": \"Run 2\", \"startTimeGMT\": \"2025-10-02 11:00:00\"}]";

        when(activityRepository.existsByDataHash(anyString())).thenReturn(false);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(syncHistoryRepository.save(any(SyncHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncActivities(activitiesJson, testUser);

        // Then
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository, times(2)).save(activityCaptor.capture());
        List<Activity> savedActivities = activityCaptor.getAllValues();

        assertEquals(2, savedActivities.size());
        assertEquals("1", savedActivities.get(0).getGarminActivityId());
        assertEquals("Run 1", savedActivities.get(0).getActivityName());
        assertNotNull(savedActivities.get(0).getDataHash());
        assertEquals(testUser, savedActivities.get(0).getUser());

        assertEquals("2", savedActivities.get(1).getGarminActivityId());
        assertEquals("Run 2", savedActivities.get(1).getActivityName());
        assertNotNull(savedActivities.get(1).getDataHash());
        assertEquals(testUser, savedActivities.get(1).getUser());

        ArgumentCaptor<SyncHistory> syncHistoryCaptor = ArgumentCaptor.forClass(SyncHistory.class);
        verify(syncHistoryRepository, times(2)).save(syncHistoryCaptor.capture());
        SyncHistory finalSyncHistory = syncHistoryCaptor.getAllValues().get(1); // Get the last saved SyncHistory

        assertEquals(2, finalSyncHistory.getActivitiesProcessed());
        assertEquals(2, finalSyncHistory.getActivitiesSynced());
        assertEquals(0, finalSyncHistory.getActivitiesSkipped());
        assertEquals(SyncHistory.SyncStatus.COMPLETED, finalSyncHistory.getSyncStatus());
    }

    @Test
    void testSyncActivities_duplicateActivities() {
        // Given
        String activitiesJson = "[{\"activityId\": \"1\", \"activityName\": \"Run 1\", \"startTimeGMT\": \"2025-10-01 10:00:00\"},"
                                 + "{\"activityId\": \"2\", \"activityName\": \"Run 2\", \"startTimeGMT\": \"2025-10-02 11:00:00\"}]";

        when(activityRepository.existsByDataHash(anyString())).thenReturn(true); // All activities are duplicates
        when(syncHistoryRepository.save(any(SyncHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncActivities(activitiesJson, testUser);

        // Then
        verify(activityRepository, never()).save(any(Activity.class)); // No activities should be saved

        ArgumentCaptor<SyncHistory> syncHistoryCaptor = ArgumentCaptor.forClass(SyncHistory.class);
        verify(syncHistoryRepository, times(2)).save(syncHistoryCaptor.capture());
        SyncHistory finalSyncHistory = syncHistoryCaptor.getAllValues().get(1);

        assertEquals(2, finalSyncHistory.getActivitiesProcessed());
        assertEquals(0, finalSyncHistory.getActivitiesSynced());
        assertEquals(2, finalSyncHistory.getActivitiesSkipped());
        assertEquals(SyncHistory.SyncStatus.COMPLETED, finalSyncHistory.getSyncStatus());
    }

    @Test
    void testSyncActivities_mixedActivities() {
        // Given
        String activitiesJson = "[{\"activityId\": \"1\", \"activityName\": \"Run 1\", \"startTimeGMT\": \"2025-10-01 10:00:00\"},"
                                 + "{\"activityId\": \"2\", \"activityName\": \"Run 2\", \"startTimeGMT\": \"2025-10-02 11:00:00\"}]";

        // Calculate expected hashes manually for mocking
        String hash1 = calculateSha256("{\"activityId\":\"1\",\"activityName\":\"Run 1\",\"startTimeGMT\":\"2025-10-01 10:00:00\"}");
        String hash2 = calculateSha256("{\"activityId\":\"2\",\"activityName\":\"Run 2\",\"startTimeGMT\":\"2025-10-02 11:00:00\"}");

        when(activityRepository.existsByDataHash(hash1)).thenReturn(false);
        when(activityRepository.existsByDataHash(hash2)).thenReturn(true);

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(syncHistoryRepository.save(any(SyncHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncActivities(activitiesJson, testUser);

        // Then
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository, times(1)).save(activityCaptor.capture()); // Only one activity should be saved
        assertEquals("1", activityCaptor.getValue().getGarminActivityId());

        ArgumentCaptor<SyncHistory> syncHistoryCaptor = ArgumentCaptor.forClass(SyncHistory.class);
        verify(syncHistoryRepository, times(2)).save(syncHistoryCaptor.capture());
        SyncHistory finalSyncHistory = syncHistoryCaptor.getAllValues().get(1);

        assertEquals(2, finalSyncHistory.getActivitiesProcessed());
        assertEquals(1, finalSyncHistory.getActivitiesSynced());
        assertEquals(1, finalSyncHistory.getActivitiesSkipped());
        assertEquals(SyncHistory.SyncStatus.COMPLETED, finalSyncHistory.getSyncStatus());
    }

    @Test
    void testSyncActivities_invalidJson() {
        // Given
        String invalidJson = "invalid json string";
        when(syncHistoryRepository.save(any(SyncHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncActivities(invalidJson, testUser);

        // Then
        verify(activityRepository, never()).save(any(Activity.class)); // No activities should be saved

        ArgumentCaptor<SyncHistory> syncHistoryCaptor = ArgumentCaptor.forClass(SyncHistory.class);
        verify(syncHistoryRepository, times(2)).save(syncHistoryCaptor.capture());
        SyncHistory finalSyncHistory = syncHistoryCaptor.getAllValues().get(1);

        assertEquals(0, finalSyncHistory.getActivitiesProcessed());
        assertEquals(0, finalSyncHistory.getActivitiesSynced());
        assertEquals(0, finalSyncHistory.getActivitiesSkipped());
        assertEquals(SyncHistory.SyncStatus.FAILED, finalSyncHistory.getSyncStatus());
        assertNotNull(finalSyncHistory.getErrorMessage());
        assertTrue(finalSyncHistory.getErrorMessage().contains("Error parsing activities JSON"));
    }

    @Test
    void testSyncActivities_exceptionDuringProcessing() {
        // Given
        String activitiesJson = "[{\"activityId\": \"1\", \"activityName\": \"Run 1\", \"startTimeGMT\": \"2025-10-01 10:00:00\"}]";
        when(activityRepository.existsByDataHash(anyString())).thenThrow(new RuntimeException("Database error"));
        when(syncHistoryRepository.save(any(SyncHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncActivities(activitiesJson, testUser);

        // Then
        verify(activityRepository, never()).save(any(Activity.class)); // No activities should be saved

        ArgumentCaptor<SyncHistory> syncHistoryCaptor = ArgumentCaptor.forClass(SyncHistory.class);
        verify(syncHistoryRepository, times(2)).save(syncHistoryCaptor.capture());
        SyncHistory finalSyncHistory = syncHistoryCaptor.getAllValues().get(1);

        assertEquals(1, finalSyncHistory.getActivitiesProcessed()); // JSON was parsed
        assertEquals(0, finalSyncHistory.getActivitiesSynced());
        assertEquals(0, finalSyncHistory.getActivitiesSkipped());
        assertEquals(SyncHistory.SyncStatus.FAILED, finalSyncHistory.getSyncStatus());
        assertNotNull(finalSyncHistory.getErrorMessage());
        assertTrue(finalSyncHistory.getErrorMessage().contains("An unexpected error occurred"));
    }

    private String calculateSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
