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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GoldenCheetahService {

    private static final Logger logger = LoggerFactory.getLogger(GoldenCheetahService.class);
    private static final String TEMP_FIT_DIR = "/tmp/golden-bridge/fits/";

    private final ActivityRepository activityRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;
    private final SyncHistoryRepository syncHistoryRepository;
    private final GarminIntegrationService garminIntegrationService;

    public GoldenCheetahService(
            ActivityRepository activityRepository,
            UserPreferencesRepository userPreferencesRepository,
            UserRepository userRepository,
            SyncHistoryRepository syncHistoryRepository,
            GarminIntegrationService garminIntegrationService) {
        this.activityRepository = activityRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.userRepository = userRepository;
        this.syncHistoryRepository = syncHistoryRepository;
        this.garminIntegrationService = garminIntegrationService;
    }

    /**
     * Export a single activity to Golden Cheetah
     */
    @Transactional
    public Activity exportActivity(Long activityId, Long userId) {
        logger.info("Starting export for activity {} for user {}", activityId, userId);

        // Get activity
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new GoldenCheetahExportException("Activity not found with id: " + activityId));

        // Verify activity belongs to user
        if (!activity.getUser().getId().equals(userId)) {
            throw new GoldenCheetahExportException("Activity does not belong to user");
        }

        // Get user preferences
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new GoldenCheetahExportException("User preferences not found"));

        // Check if golden_cheetah_path is set
        if (preferences.getGoldenCheetahPath() == null || preferences.getGoldenCheetahPath().isBlank()) {
            throw new GoldenCheetahExportException("Golden Cheetah path not configured. Please set it in user preferences.");
        }

        // Get user for username
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GoldenCheetahExportException("User not found"));

        // Download FIT file if not already downloaded
        Path fitFilePath;
        if (activity.getFitFilePath() == null || activity.getFitFilePath().isBlank()) {
            logger.info("FIT file not downloaded yet for activity {}. Downloading...", activityId);
            try {
                fitFilePath = downloadAndSaveFitFile(activity);
                activity.setFitFilePath(fitFilePath.toString());
                activityRepository.save(activity);
            } catch (ActivityDownloadException e) {
                logger.error("Failed to download FIT file for activity {}: {}", activityId, e.getMessage());
                activity.setSyncError("FIT download failed: " + e.getMessage());
                activity.setSyncStatus(Activity.SyncStatus.FAILED);
                activity.setLastSyncAttempt(LocalDateTime.now());
                activityRepository.save(activity);
                throw new GoldenCheetahExportException("Failed to download FIT file: " + e.getMessage(), e);
            }
        } else {
            fitFilePath = Paths.get(activity.getFitFilePath());
            if (!Files.exists(fitFilePath)) {
                logger.warn("FIT file path exists in DB but file not found: {}. Re-downloading...", fitFilePath);
                try {
                    fitFilePath = downloadAndSaveFitFile(activity);
                    activity.setFitFilePath(fitFilePath.toString());
                    activityRepository.save(activity);
                } catch (ActivityDownloadException e) {
                    logger.error("Failed to re-download FIT file for activity {}: {}", activityId, e.getMessage());
                    throw new GoldenCheetahExportException("Failed to re-download FIT file: " + e.getMessage(), e);
                }
            }
        }

        // Copy FIT file to Golden Cheetah directory
        try {
            Path gcDestination = copyToGoldenCheetah(fitFilePath, preferences.getGoldenCheetahPath(), user.getUsername());
            activity.setGoldenCheetahPath(gcDestination.toString());
            activity.setSyncStatus(Activity.SyncStatus.EXPORTED);
            activity.setLastSyncAttempt(LocalDateTime.now());
            activity.setSyncError(null);
            activityRepository.save(activity);

            // Create sync history entry
            createSyncHistoryEntry(user, 1, 1, 0, 0, null);

            logger.info("Successfully exported activity {} to {}", activityId, gcDestination);
            return activity;

        } catch (IOException e) {
            logger.error("Failed to copy FIT file to Golden Cheetah directory: {}", e.getMessage());
            activity.setSyncError("File copy failed: " + e.getMessage());
            activity.setSyncStatus(Activity.SyncStatus.FAILED);
            activity.setLastSyncAttempt(LocalDateTime.now());
            activityRepository.save(activity);
            throw new GoldenCheetahExportException("Failed to copy file to Golden Cheetah: " + e.getMessage(), e);
        }
    }

    /**
     * Export all activities for a user
     */
    @Transactional
    public List<Activity> exportAllActivities(Long userId) {
        logger.info("Starting export of all activities for user {}", userId);

        // Get user preferences
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new GoldenCheetahExportException("User preferences not found"));

        // Check if golden_cheetah_path is set
        if (preferences.getGoldenCheetahPath() == null || preferences.getGoldenCheetahPath().isBlank()) {
            throw new GoldenCheetahExportException("Golden Cheetah path not configured. Please set it in user preferences.");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GoldenCheetahExportException("User not found"));

        // Get all activities for user that are not yet exported
        List<Activity> activities = activityRepository.findByUserIdAndSyncStatusNot(userId, Activity.SyncStatus.EXPORTED);

        if (activities.isEmpty()) {
            logger.info("No activities to export for user {}", userId);
            return activities;
        }

        List<Activity> exportedActivities = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Activity activity : activities) {
            try {
                Activity exported = exportSingleActivityInternal(activity, preferences, user);
                exportedActivities.add(exported);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to export activity {}: {}", activity.getId(), e.getMessage());
                failureCount++;
                errors.append(String.format("Activity %d: %s; ", activity.getId(), e.getMessage()));

                // Update activity status
                activity.setSyncError("Export failed: " + e.getMessage());
                activity.setSyncStatus(Activity.SyncStatus.FAILED);
                activity.setLastSyncAttempt(LocalDateTime.now());
                activityRepository.save(activity);
            }
        }

        // Create sync history entry
        String errorMessage = failureCount > 0 ? errors.toString() : null;
        createSyncHistoryEntry(user, activities.size(), successCount, 0, failureCount, errorMessage);

        logger.info("Export completed for user {}. Success: {}, Failed: {}", userId, successCount, failureCount);

        if (failureCount > 0 && successCount == 0) {
            throw new GoldenCheetahExportException("All exports failed. Errors: " + errorMessage);
        }

        return exportedActivities;
    }

    /**
     * Internal method to export a single activity (used by exportAllActivities)
     */
    private Activity exportSingleActivityInternal(Activity activity, UserPreferences preferences, User user) {
        // Download FIT file if not already downloaded
        Path fitFilePath;
        if (activity.getFitFilePath() == null || activity.getFitFilePath().isBlank()) {
            fitFilePath = downloadAndSaveFitFile(activity);
            activity.setFitFilePath(fitFilePath.toString());
            activityRepository.save(activity);
        } else {
            fitFilePath = Paths.get(activity.getFitFilePath());
            if (!Files.exists(fitFilePath)) {
                fitFilePath = downloadAndSaveFitFile(activity);
                activity.setFitFilePath(fitFilePath.toString());
                activityRepository.save(activity);
            }
        }

        // Copy to Golden Cheetah
        try {
            Path gcDestination = copyToGoldenCheetah(fitFilePath, preferences.getGoldenCheetahPath(), user.getUsername());
            activity.setGoldenCheetahPath(gcDestination.toString());
            activity.setSyncStatus(Activity.SyncStatus.EXPORTED);
            activity.setLastSyncAttempt(LocalDateTime.now());
            activity.setSyncError(null);
            return activityRepository.save(activity);
        } catch (IOException e) {
            throw new GoldenCheetahExportException("Failed to copy file: " + e.getMessage(), e);
        }
    }

    /**
     * Download FIT file from Garmin and save to temp location
     */
    private Path downloadAndSaveFitFile(Activity activity) {
        byte[] fitData = garminIntegrationService.downloadActivityFitFile(activity.getGarminActivityId());

        try {
            // Create temp directory if it doesn't exist
            Path tempDir = Paths.get(TEMP_FIT_DIR);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // Save FIT file
            String filename = String.format("%s_%s.fit",
                    activity.getGarminActivityId(),
                    activity.getActivityDate().toLocalDate().toString());
            Path fitFilePath = tempDir.resolve(filename);
            Files.write(fitFilePath, fitData);

            logger.info("FIT file saved to: {}", fitFilePath);
            return fitFilePath;

        } catch (IOException e) {
            logger.error("Failed to save FIT file: {}", e.getMessage());
            throw new GoldenCheetahExportException("Failed to save FIT file: " + e.getMessage(), e);
        }
    }

    /**
     * Copy FIT file to Golden Cheetah directory structure
     */
    private Path copyToGoldenCheetah(Path sourceFitFile, String gcBasePath, String username) throws IOException {
        // Golden Cheetah structure: {basePath}/{username}/activities/
        Path gcActivitiesDir = Paths.get(gcBasePath, username, "activities");

        // Create directories if they don't exist
        if (!Files.exists(gcActivitiesDir)) {
            Files.createDirectories(gcActivitiesDir);
            logger.info("Created Golden Cheetah directory: {}", gcActivitiesDir);
        }

        // Copy file
        Path destination = gcActivitiesDir.resolve(sourceFitFile.getFileName());
        Files.copy(sourceFitFile, destination, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Copied FIT file from {} to {}", sourceFitFile, destination);
        return destination;
    }

    /**
     * Create a sync history entry
     */
    private void createSyncHistoryEntry(User user, int processed, int synced, int skipped, int failed, String errorMessage) {
        SyncHistory syncHistory = new SyncHistory(SyncHistory.SyncType.MANUAL, user);
        syncHistory.setSyncStatus(failed > 0 ? SyncHistory.SyncStatus.COMPLETED : SyncHistory.SyncStatus.COMPLETED);
        syncHistory.setSyncCompletedAt(LocalDateTime.now());
        syncHistory.setActivitiesProcessed(processed);
        syncHistory.setActivitiesSynced(synced);
        syncHistory.setActivitiesSkipped(skipped);
        syncHistory.setActivitiesFailed(failed);
        syncHistory.setErrorMessage(errorMessage);
        syncHistory.setSyncDetails("Golden Cheetah export");
        syncHistoryRepository.save(syncHistory);
    }
}
