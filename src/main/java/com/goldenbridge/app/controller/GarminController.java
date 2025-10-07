package com.goldenbridge.app.controller;

import com.goldenbridge.app.exception.ActivityDownloadException;
import com.goldenbridge.app.service.GarminIntegrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/garmin")
public class GarminController {

    private final GarminIntegrationService garminIntegrationService;

    public GarminController(GarminIntegrationService garminIntegrationService) {
        this.garminIntegrationService = garminIntegrationService;
    }

    @GetMapping("/activities")
    public ResponseEntity<String> getGarminActivities(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int limit) {

        String activities = garminIntegrationService.getGarminActivities(start, limit);
        return ResponseEntity.ok(activities);
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
