package com.goldenbridge.app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "sync_history", indexes = {
    @Index(name = "idx_sync_history_user_date", columnList = "user_id, sync_started_at"),
    @Index(name = "idx_sync_history_status", columnList = "sync_status")
})
@EntityListeners(AuditingEntityListener.class)
public class SyncHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false)
    private SyncType syncType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.STARTED;
    
    @NotNull(message = "Sync start time is required")
    @Column(name = "sync_started_at", nullable = false)
    private LocalDateTime syncStartedAt;
    
    @Column(name = "sync_completed_at")
    private LocalDateTime syncCompletedAt;
    
    @Column(name = "activities_processed")
    private Integer activitiesProcessed = 0;
    
    @Column(name = "activities_synced")
    private Integer activitiesSynced = 0;
    
    @Column(name = "activities_skipped")
    private Integer activitiesSkipped = 0;
    
    @Column(name = "activities_failed")
    private Integer activitiesFailed = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "sync_details", columnDefinition = "TEXT")
    private String syncDetails;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Enums
    public enum SyncType {
        MANUAL, SCHEDULED, PARTIAL, FULL_RESYNC
    }
    
    public enum SyncStatus {
        STARTED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }
    
    // Constructors
    public SyncHistory() {}
    
    public SyncHistory(SyncType syncType, User user) {
        this.syncType = syncType;
        this.user = user;
        this.syncStartedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SyncType getSyncType() { return syncType; }
    public void setSyncType(SyncType syncType) { this.syncType = syncType; }
    
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    
    public LocalDateTime getSyncStartedAt() { return syncStartedAt; }
    public void setSyncStartedAt(LocalDateTime syncStartedAt) { this.syncStartedAt = syncStartedAt; }
    
    public LocalDateTime getSyncCompletedAt() { return syncCompletedAt; }
    public void setSyncCompletedAt(LocalDateTime syncCompletedAt) { this.syncCompletedAt = syncCompletedAt; }
    
    public Integer getActivitiesProcessed() { return activitiesProcessed; }
    public void setActivitiesProcessed(Integer activitiesProcessed) { this.activitiesProcessed = activitiesProcessed; }
    
    public Integer getActivitiesSynced() { return activitiesSynced; }
    public void setActivitiesSynced(Integer activitiesSynced) { this.activitiesSynced = activitiesSynced; }
    
    public Integer getActivitiesSkipped() { return activitiesSkipped; }
    public void setActivitiesSkipped(Integer activitiesSkipped) { this.activitiesSkipped = activitiesSkipped; }
    
    public Integer getActivitiesFailed() { return activitiesFailed; }
    public void setActivitiesFailed(Integer activitiesFailed) { this.activitiesFailed = activitiesFailed; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getSyncDetails() { return syncDetails; }
    public void setSyncDetails(String syncDetails) { this.syncDetails = syncDetails; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}