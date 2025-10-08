package com.goldenbridge.app.controller;

import com.goldenbridge.app.entity.Activity;
import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.exception.GoldenCheetahExportException;
import com.goldenbridge.app.repository.UserRepository;
import com.goldenbridge.app.service.GoldenCheetahService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/golden-cheetah")
public class GoldenCheetahController {

    private static final Logger logger = LoggerFactory.getLogger(GoldenCheetahController.class);

    private final GoldenCheetahService goldenCheetahService;
    private final UserRepository userRepository;

    public GoldenCheetahController(GoldenCheetahService goldenCheetahService, UserRepository userRepository) {
        this.goldenCheetahService = goldenCheetahService;
        this.userRepository = userRepository;
    }

    /**
     * Export a single activity to Golden Cheetah
     */
    @PostMapping("/export/{activityId}")
    public ResponseEntity<?> exportActivity(
            @PathVariable Long activityId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Activity exportedActivity = goldenCheetahService.exportActivity(activityId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Activity exported successfully");
            response.put("activityId", exportedActivity.getId());
            response.put("activityName", exportedActivity.getActivityName());
            response.put("goldenCheetahPath", exportedActivity.getGoldenCheetahPath());
            response.put("syncStatus", exportedActivity.getSyncStatus());

            logger.info("Successfully exported activity {} for user {}", activityId, user.getUsername());
            return ResponseEntity.ok(response);

        } catch (GoldenCheetahExportException e) {
            logger.error("Export failed: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            // Return 400 for user configuration errors
            if (e.getMessage().contains("not configured") || e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during export: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Export all activities for the authenticated user
     */
    @PostMapping("/export-all")
    public ResponseEntity<?> exportAllActivities(@AuthenticationPrincipal UserDetails userDetails) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Activity> exportedActivities = goldenCheetahService.exportAllActivities(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Export completed");
            response.put("totalExported", exportedActivities.size());
            response.put("activities", exportedActivities.stream()
                    .map(activity -> Map.of(
                            "id", activity.getId(),
                            "name", activity.getActivityName(),
                            "syncStatus", activity.getSyncStatus(),
                            "goldenCheetahPath", activity.getGoldenCheetahPath() != null ? activity.getGoldenCheetahPath() : "N/A"
                    ))
                    .toList());

            logger.info("Successfully exported {} activities for user {}", exportedActivities.size(), user.getUsername());
            return ResponseEntity.ok(response);

        } catch (GoldenCheetahExportException e) {
            logger.error("Export all failed: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            // Return 400 for user configuration errors
            if (e.getMessage().contains("not configured") || e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during export all: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
