package com.goldenbridge.app.controller;

import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.exception.ActivityDownloadException;
import com.goldenbridge.app.repository.UserRepository;
import com.goldenbridge.app.service.GarminIntegrationService;
import com.goldenbridge.app.service.SyncService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/garmin")
public class GarminController {

    private final GarminIntegrationService garminIntegrationService;
    private final SyncService syncService;
    private final UserRepository userRepository;

    public GarminController(GarminIntegrationService garminIntegrationService, SyncService syncService, UserRepository userRepository) {
        this.garminIntegrationService = garminIntegrationService;
        this.syncService = syncService;
        this.userRepository = userRepository;
    }

    @GetMapping("/activities")
    public ResponseEntity<String> getGarminActivities(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        String activitiesJson = garminIntegrationService.getGarminActivities(start, limit);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        syncService.syncActivities(activitiesJson, user);

        return ResponseEntity.ok("Sync process initiated. Activities are being saved to the database.");
    }

    @GetMapping("/activities/{activityId}/download")
    public ResponseEntity<byte[]> downloadActivityFitFile(@PathVariable String activityId) {
        try {
            byte[] fitFile = garminIntegrationService.downloadActivityFitFile(activityId);

            if (fitFile == null || fitFile.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("No FIT file found for activity " + activityId).getBytes());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", activityId + ".fit");
            headers.setContentLength(fitFile.length);

            return new ResponseEntity<>(fitFile, headers, HttpStatus.OK);

        } catch (ActivityDownloadException e) {
            System.err.println("Error downloading FIT file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error downloading FIT file: " + e.getMessage()).getBytes());

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }
}
