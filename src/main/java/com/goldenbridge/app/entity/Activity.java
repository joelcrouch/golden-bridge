package com.goldenbridge.app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_garmin_id", columnList = "garmin_activity_id"),
    @Index(name = "idx_activity_user_date", columnList = "user_id, activity_date"),
    @Index(name = "idx_activity_sync_status", columnList = "sync_status")
})
@EntityListeners(AuditingEntityListener.class)
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Garmin activity ID is required")
    @Column(name = "garmin_activity_id", nullable = false, unique = true)
    private String garminActivityId;
    
    @NotBlank(message = "Activity name is required")
    @Column(name = "activity_name", nullable = false)
    private String activityName;
    
    @Column(name = "activity_type", length = 50)
    private String activityType;
    
    @NotNull(message = "Activity date is required")
    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "distance_meters", precision = 10, scale = 2)
    private BigDecimal distanceMeters;
    
    @Column(name = "calories")
    private Integer calories;
    
    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;
    
    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;
    
    @Column(name = "average_power")
    private Integer averagePower;
    
    @Column(name = "max_power")
    private Integer maxPower;
    
    @Column(name = "elevation_gain_meters", precision = 8, scale = 2)
    private BigDecimal elevationGainMeters;
    
    @Column(name = "average_speed_kmh", precision = 6, scale = 2)
    private BigDecimal averageSpeedKmh;
    
    @Column(name = "max_speed_kmh", precision = 6, scale = 2)
    private BigDecimal maxSpeedKmh;
    
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;
    
    @Column(name = "fit_file_path")
    private String fitFilePath;
    
    @Column(name = "gpx_file_path")
    private String gpxFilePath;
    
    @Column(name = "data_hash", length = 64)
    private String dataHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
    @Column(name = "sync_error")
    private String syncError;
    
    @Column(name = "last_sync_attempt")
    private LocalDateTime lastSyncAttempt;
    
    @Column(name = "golden_cheetah_path")
    private String goldenCheetahPath;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Enum for sync status
    public enum SyncStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
    }
    
    // Constructors
    public Activity() {}
    
    public Activity(String garminActivityId, String activityName, LocalDateTime activityDate, User user) {
        this.garminActivityId = garminActivityId;
        this.activityName = activityName;
        this.activityDate = activityDate;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getGarminActivityId() { return garminActivityId; }
    public void setGarminActivityId(String garminActivityId) { this.garminActivityId = garminActivityId; }
    
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    
    public LocalDateTime getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDateTime activityDate) { this.activityDate = activityDate; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public BigDecimal getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(BigDecimal distanceMeters) { this.distanceMeters = distanceMeters; }
    
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Add remaining getters/setters for brevity - you can add them as needed
}