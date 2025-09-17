package com.goldenbridge.app.repository;

import com.goldenbridge.app.entity.UserPreferences;
import com.goldenbridge.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    
    Optional<UserPreferences> findByUser(User user);
    
    Optional<UserPreferences> findByUserId(Long userId);
    
    List<UserPreferences> findByAutoSyncEnabledTrue();
    
    @Query("SELECT up FROM UserPreferences up WHERE up.autoSyncEnabled = true AND up.user.isActive = true AND up.user.syncEnabled = true")
    List<UserPreferences> findActiveAutoSyncPreferences();
    
    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.autoSyncEnabled = true")
    Long countAutoSyncEnabledUsers();
}
