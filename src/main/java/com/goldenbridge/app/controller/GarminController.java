package com.goldenbridge.app.controller;

import com.goldenbridge.app.service.GarminIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<String> getGarminActivities(@RequestParam(defaultValue = "0") int start, @RequestParam(defaultValue = "10") int limit) {
        // This assumes the user has already logged in to Garmin via the /garmin/login endpoint in AuthController
        String activities = garminIntegrationService.getGarminActivities(start, limit);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activity_detail/{activityId}")
    public ResponseEntity<String> getGarminActivityDetails(@PathVariable long activityId) {
        String activityDetails = garminIntegrationService.getGarminActivityDetails(activityId);
        return ResponseEntity.ok(activityDetails);
    }
}
