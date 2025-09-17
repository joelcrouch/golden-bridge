package com.goldenbridge.app.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@EntityListeners(AuditingEntityListener.class)
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "auto_sync_enabled", nullable = false)
    private Boolean autoSyncEnabled = false;
    
    @Column(name = "sync_frequency_hours", nullable = false)
    private Integer syncFrequencyHours = 24;
    
    @Column(name = "sync_activity_types")
    private String syncActivityTypes = "cycling,running,swimming";
    
    @Column(name = "golden_cheetah_path")
    private String goldenCheetahPath;
    
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;
    
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = false;
    
    @Column(name = "max_sync_days", nullable = false)
    private Integer maxSyncDays = 30;
    
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // Constructors
    public UserPreferences() {}
    
    public UserPreferences(User user) {
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Boolean getAutoSyncEnabled() { return autoSyncEnabled; }
    public void setAutoSyncEnabled(Boolean autoSyncEnabled) { this.autoSyncEnabled = autoSyncEnabled; }
    
    public Integer getSyncFrequencyHours() { return syncFrequencyHours; }
    public void setSyncFrequencyHours(Integer syncFrequencyHours) { this.syncFrequencyHours = syncFrequencyHours; }
    
    public String getSyncActivityTypes() { return syncActivityTypes; }
    public void setSyncActivityTypes(String syncActivityTypes) { this.syncActivityTypes = syncActivityTypes; }
    
    public String getGoldenCheetahPath() { return goldenCheetahPath; }
    public void setGoldenCheetahPath(String goldenCheetahPath) { this.goldenCheetahPath = goldenCheetahPath; }
    
    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    
    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }
    
    public Integer getMaxSyncDays() { return maxSyncDays; }
    public void setMaxSyncDays(Integer maxSyncDays) { this.maxSyncDays = maxSyncDays; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}