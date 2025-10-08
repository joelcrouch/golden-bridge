package com.goldenbridge.app.service;

import com.goldenbridge.app.entity.Activity;
import com.goldenbridge.app.entity.SyncHistory;
import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.entity.UserPreferences;
import com.goldenbridge.app.exception.ActivityDownloadException;
import com.goldenbridge.app.exception.GoldenCheetahExportException;
import com.goldenbridge.app.repository.ActivityRepository;
import com.goldenbridge.app.repository.SyncHistoryRepository;
import com.goldenbridge.app.repository.UserPreferencesRepository;
import com.goldenbridge.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoldenCheetahServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SyncHistoryRepository syncHistoryRepository;

    @Mock
    private GarminIntegrationService garminIntegrationService;

    @InjectMocks
    private GoldenCheetahService goldenCheetahService;

    @TempDir
    Path tempDir;

    private User testUser;
    private Activity testActivity;
    private UserPreferences testPreferences;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Setup test activity
        testActivity = new Activity();
        testActivity.setId(100L);
        testActivity.setGarminActivityId("123456");
        testActivity.setActivityName("Morning Ride");
        testActivity.setActivityDate(LocalDateTime.now());
        testActivity.setSyncStatus(Activity.SyncStatus.COMPLETED);
        testActivity.setUser(testUser);

        // Setup test preferences
        testPreferences = new UserPreferences();
        testPreferences.setId(1L);
        testPreferences.setUser(testUser);
        testPreferences.setGoldenCheetahPath(tempDir.toString());
    }

    @Test
    void exportActivity_Success_WithExistingFitFile() throws IOException {
        // Given
        Path fitFilePath = tempDir.resolve("123456_2025-01-01.fit");
        Files.write(fitFilePath, "FIT file content".getBytes());
        testActivity.setFitFilePath(fitFilePath.toString());

        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Activity result = goldenCheetahService.exportActivity(100L, 1L);

        // Then
        assertThat(result.getSyncStatus()).isEqualTo(Activity.SyncStatus.EXPORTED);
        assertThat(result.getGoldenCheetahPath()).contains("testuser/activities");
        assertThat(result.getSyncError()).isNull();

        // Verify Golden Cheetah directory structure was created
        Path gcActivitiesDir = tempDir.resolve("testuser/activities");
        assertThat(Files.exists(gcActivitiesDir)).isTrue();

        // Verify FIT file was copied
        Path copiedFitFile = gcActivitiesDir.resolve(fitFilePath.getFileName());
        assertThat(Files.exists(copiedFitFile)).isTrue();

        verify(activityRepository, times(1)).save(any(Activity.class));
        verify(syncHistoryRepository, times(1)).save(any(SyncHistory.class));
        verify(garminIntegrationService, never()).downloadActivityFitFile(anyString());
    }

    @Test
    void exportActivity_Success_DownloadingFitFile() {
        // Given
        testActivity.setFitFilePath(null); // No FIT file yet

        byte[] fitData = "FIT file content".getBytes();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(garminIntegrationService.downloadActivityFitFile("123456")).thenReturn(fitData);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Activity result = goldenCheetahService.exportActivity(100L, 1L);

        // Then
        assertThat(result.getSyncStatus()).isEqualTo(Activity.SyncStatus.EXPORTED);
        assertThat(result.getFitFilePath()).isNotNull();
        assertThat(result.getGoldenCheetahPath()).contains("testuser/activities");

        verify(garminIntegrationService, times(1)).downloadActivityFitFile("123456");
        verify(activityRepository, times(2)).save(any(Activity.class)); // Once for FIT path, once for GC path
        verify(syncHistoryRepository, times(1)).save(any(SyncHistory.class));
    }

    @Test
    void exportActivity_ActivityNotFound() {
        // Given
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportActivity(999L, 1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("Activity not found");

        verify(activityRepository, never()).save(any(Activity.class));
        verify(syncHistoryRepository, never()).save(any(SyncHistory.class));
    }

    @Test
    void exportActivity_ActivityDoesNotBelongToUser() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        testActivity.setUser(otherUser);

        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportActivity(100L, 1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("does not belong to user");

        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void exportActivity_UserPreferencesNotFound() {
        // Given
        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportActivity(100L, 1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("User preferences not found");

        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void exportActivity_GoldenCheetahPathNotConfigured() {
        // Given
        testPreferences.setGoldenCheetahPath(null);

        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportActivity(100L, 1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("Golden Cheetah path not configured");

        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void exportActivity_GoldenCheetahPathBlank() {
        // Given
        testPreferences.setGoldenCheetahPath("   ");

        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportActivity(100L, 1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("Golden Cheetah path not configured");
    }

    @Test
    void exportActivity_FitDownloadFails() {
        // Given
        testActivity.setFitFilePath(null);

        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(garminIntegrationService.downloadActivityFitFile("123456"))
                .thenThrow(new ActivityDownloadException("Download failed"));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportActivity(100L, 1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("Failed to download FIT file");

        // Verify activity was updated with error
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository, atLeastOnce()).save(activityCaptor.capture());

        Activity savedActivity = activityCaptor.getValue();
        assertThat(savedActivity.getSyncStatus()).isEqualTo(Activity.SyncStatus.FAILED);
        assertThat(savedActivity.getSyncError()).contains("FIT download failed");
    }

    @Test
    void exportActivity_FitFilePathExistsButFileNotFound() {
        // Given
        testActivity.setFitFilePath("/nonexistent/file.fit");

        byte[] fitData = "FIT file content".getBytes();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(testActivity));
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(garminIntegrationService.downloadActivityFitFile("123456")).thenReturn(fitData);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Activity result = goldenCheetahService.exportActivity(100L, 1L);

        // Then - should re-download and succeed
        assertThat(result.getSyncStatus()).isEqualTo(Activity.SyncStatus.EXPORTED);
        verify(garminIntegrationService, times(1)).downloadActivityFitFile("123456");
    }

    @Test
    void exportAllActivities_Success() {
        // Given
        Activity activity1 = new Activity();
        activity1.setId(101L);
        activity1.setGarminActivityId("111");
        activity1.setActivityName("Activity 1");
        activity1.setActivityDate(LocalDateTime.now());
        activity1.setSyncStatus(Activity.SyncStatus.COMPLETED);
        activity1.setUser(testUser);

        Activity activity2 = new Activity();
        activity2.setId(102L);
        activity2.setGarminActivityId("222");
        activity2.setActivityName("Activity 2");
        activity2.setActivityDate(LocalDateTime.now());
        activity2.setSyncStatus(Activity.SyncStatus.PENDING);
        activity2.setUser(testUser);

        List<Activity> activities = Arrays.asList(activity1, activity2);

        byte[] fitData = "FIT file content".getBytes();

        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndSyncStatusNot(1L, Activity.SyncStatus.EXPORTED))
                .thenReturn(activities);
        when(garminIntegrationService.downloadActivityFitFile(anyString())).thenReturn(fitData);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Activity> result = goldenCheetahService.exportAllActivities(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getSyncStatus() == Activity.SyncStatus.EXPORTED);

        verify(garminIntegrationService, times(2)).downloadActivityFitFile(anyString());
        verify(activityRepository, atLeast(2)).save(any(Activity.class));
        verify(syncHistoryRepository, times(1)).save(any(SyncHistory.class));
    }

    @Test
    void exportAllActivities_NoActivitiesToExport() {
        // Given
        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndSyncStatusNot(1L, Activity.SyncStatus.EXPORTED))
                .thenReturn(List.of());

        // When
        List<Activity> result = goldenCheetahService.exportAllActivities(1L);

        // Then
        assertThat(result).isEmpty();
        verify(garminIntegrationService, never()).downloadActivityFitFile(anyString());
        verify(syncHistoryRepository, never()).save(any(SyncHistory.class));
    }

    @Test
    void exportAllActivities_PartialFailure() {
        // Given
        Activity activity1 = new Activity();
        activity1.setId(101L);
        activity1.setGarminActivityId("111");
        activity1.setActivityName("Activity 1");
        activity1.setActivityDate(LocalDateTime.now());
        activity1.setSyncStatus(Activity.SyncStatus.COMPLETED);
        activity1.setUser(testUser);

        Activity activity2 = new Activity();
        activity2.setId(102L);
        activity2.setGarminActivityId("222");
        activity2.setActivityName("Activity 2");
        activity2.setActivityDate(LocalDateTime.now());
        activity2.setSyncStatus(Activity.SyncStatus.PENDING);
        activity2.setUser(testUser);

        List<Activity> activities = Arrays.asList(activity1, activity2);

        byte[] fitData = "FIT file content".getBytes();

        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndSyncStatusNot(1L, Activity.SyncStatus.EXPORTED))
                .thenReturn(activities);

        // First succeeds, second fails
        when(garminIntegrationService.downloadActivityFitFile("111")).thenReturn(fitData);
        when(garminIntegrationService.downloadActivityFitFile("222"))
                .thenThrow(new ActivityDownloadException("Download failed"));

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Activity> result = goldenCheetahService.exportAllActivities(1L);

        // Then
        assertThat(result).hasSize(1); // Only successful export
        assertThat(result.get(0).getId()).isEqualTo(101L);

        // Verify sync history was saved with failure count
        ArgumentCaptor<SyncHistory> historyCaptor = ArgumentCaptor.forClass(SyncHistory.class);
        verify(syncHistoryRepository, times(1)).save(historyCaptor.capture());

        SyncHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getActivitiesProcessed()).isEqualTo(2);
        assertThat(savedHistory.getActivitiesSynced()).isEqualTo(1);
        assertThat(savedHistory.getActivitiesFailed()).isEqualTo(1);
        assertThat(savedHistory.getErrorMessage()).isNotNull();
    }

    @Test
    void exportAllActivities_AllFailures() {
        // Given
        Activity activity1 = new Activity();
        activity1.setId(101L);
        activity1.setGarminActivityId("111");
        activity1.setActivityName("Activity 1");
        activity1.setActivityDate(LocalDateTime.now());
        activity1.setSyncStatus(Activity.SyncStatus.COMPLETED);
        activity1.setUser(testUser);

        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndSyncStatusNot(1L, Activity.SyncStatus.EXPORTED))
                .thenReturn(List.of(activity1));
        when(garminIntegrationService.downloadActivityFitFile("111"))
                .thenThrow(new ActivityDownloadException("Download failed"));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportAllActivities(1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("All exports failed");
    }

    @Test
    void exportAllActivities_GoldenCheetahPathNotConfigured() {
        // Given
        testPreferences.setGoldenCheetahPath(null);

        when(userPreferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));

        // When/Then
        assertThatThrownBy(() -> goldenCheetahService.exportAllActivities(1L))
                .isInstanceOf(GoldenCheetahExportException.class)
                .hasMessageContaining("Golden Cheetah path not configured");
    }
}
