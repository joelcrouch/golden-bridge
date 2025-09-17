package com.goldenbridge.app.repository;

import com.goldenbridge.app.entity.SyncHistory;
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
public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {
    
    List<SyncHistory> findByUser(User user);
    
    Page<SyncHistory> findByUser(User user, Pageable pageable);
    
    List<SyncHistory> findByUserOrderBySyncStartedAtDesc(User user);
    
    Optional<SyncHistory> findFirstByUserOrderBySyncStartedAtDesc(User user);
    
    List<SyncHistory> findBySyncStatus(SyncHistory.SyncStatus syncStatus);
    
    @Query("SELECT sh FROM SyncHistory sh WHERE sh.user = :user AND sh.syncStartedAt >= :fromDate ORDER BY sh.syncStartedAt DESC")
    List<SyncHistory> findRecentSyncHistoryByUser(@Param("user") User user, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(sh) FROM SyncHistory sh WHERE sh.user = :user AND sh.syncStatus = :status")
    Long countByUserAndSyncStatus(@Param("user") User user, @Param("status") SyncHistory.SyncStatus status);
    
    @Query("SELECT sh.syncStatus, COUNT(sh) FROM SyncHistory sh WHERE sh.user = :user GROUP BY sh.syncStatus")
    List<Object[]> getSyncStatusStatsByUser(@Param("user") User user);
    
    @Query("SELECT sh FROM SyncHistory sh WHERE sh.syncStatus IN ('STARTED', 'IN_PROGRESS') AND sh.syncStartedAt < :cutoffTime")
    List<SyncHistory> findStuckSyncs(@Param("cutoffTime") LocalDateTime cutoffTime);
}