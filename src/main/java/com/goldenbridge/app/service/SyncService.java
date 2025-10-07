package com.goldenbridge.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goldenbridge.app.entity.Activity;
import com.goldenbridge.app.entity.SyncHistory;
import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.repository.ActivityRepository;
import com.goldenbridge.app.repository.SyncHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private final ActivityRepository activityRepository;
    private final SyncHistoryRepository syncHistoryRepository;
    private final ObjectMapper objectMapper;

    public SyncService(ActivityRepository activityRepository, SyncHistoryRepository syncHistoryRepository) {
        this.activityRepository = activityRepository;
        this.syncHistoryRepository = syncHistoryRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void syncActivities(String activitiesJson, User user) {
        SyncHistory syncHistory = new SyncHistory(SyncHistory.SyncType.MANUAL, user);
        syncHistoryRepository.save(syncHistory);

        try {
            List<Map<String, Object>> activities = objectMapper.readValue(activitiesJson, new TypeReference<>() {});
            syncHistory.setActivitiesProcessed(activities.size());

            int syncedCount = 0;
            int skippedCount = 0;

            for (Map<String, Object> activityData : activities) {
                String rawData = objectMapper.writeValueAsString(activityData);
                String dataHash = calculateSha256(rawData);

                if (!activityRepository.existsByDataHash(dataHash)) {
                    Activity activity = new Activity();
                    activity.setUser(user);
                    activity.setRawData(rawData);
                    activity.setDataHash(dataHash);
                    activity.setGarminActivityId(String.valueOf(activityData.get("activityId")));
                    activity.setActivityName((String) activityData.get("activityName"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    activity.setActivityDate(LocalDateTime.parse((String) activityData.get("startTimeGMT"), formatter));
                    // TODO: Map other fields from activityData to the Activity entity

                    activityRepository.save(activity);
                    syncedCount++;
                } else {
                    logger.info("Skipping duplicate activity with ID: {}", activityData.get("activityId"));
                    skippedCount++;
                }
            }

            syncHistory.setActivitiesSynced(syncedCount);
            syncHistory.setActivitiesSkipped(skippedCount);
            syncHistory.setSyncStatus(SyncHistory.SyncStatus.COMPLETED);

        } catch (JsonProcessingException e) {
            logger.error("Error parsing activities JSON", e);
            syncHistory.setSyncStatus(SyncHistory.SyncStatus.FAILED);
            syncHistory.setErrorMessage("Error parsing activities JSON: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred during sync", e);
            syncHistory.setSyncStatus(SyncHistory.SyncStatus.FAILED);
            syncHistory.setErrorMessage("An unexpected error occurred: " + e.getMessage());
        }

        syncHistory.setSyncCompletedAt(LocalDateTime.now());
        syncHistoryRepository.save(syncHistory);
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