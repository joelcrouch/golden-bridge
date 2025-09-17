package com.goldenbridge.app.repository;

import com.goldenbridge.app.entity.Activity;
import com.goldenbridge.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    Optional<Activity> findByGarminActivityId(String garminActivityId);
    
    List<Activity> findByUser(User user);
    
    Page<Activity> findByUser(User user, Pageable pageable);
    
    List<Activity> findByUserAndSyncStatus(User user, Activity.SyncStatus syncStatus);
    
    List<Activity> findByUserAndActivityDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT a FROM Activity a WHERE a.user = :user AND a.activityDate >= :fromDate ORDER BY a.activityDate DESC")
    List<Activity> findRecentActivitiesByUser(@Param("user") User user, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT a FROM Activity a WHERE a.syncStatus = :status AND a.lastSyncAttempt < :cutoffTime")
    List<Activity> findActivitiesForRetry(@Param("status") Activity.SyncStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.user = :user AND a.syncStatus = :status")
    Long countByUserAndSyncStatus(@Param("user") User user, @Param("status") Activity.SyncStatus status);
    
    @Query("SELECT a.activityType, COUNT(a) FROM Activity a WHERE a.user = :user GROUP BY a.activityType")
    List<Object[]> getActivityTypeStatsByUser(@Param("user") User user);
    
    boolean existsByGarminActivityId(String garminActivityId);
    
    @Query("SELECT a FROM Activity a WHERE a.user = :user AND a.syncStatus = 'PENDING' ORDER BY a.activityDate ASC")
    List<Activity> findPendingSyncActivitiesByUser(@Param("user") User user, Pageable pageable);
}