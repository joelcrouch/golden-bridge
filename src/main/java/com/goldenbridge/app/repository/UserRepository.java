package com.goldenbridge.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.goldenbridge.app.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByIsActiveTrue();
    
    List<User> findBySyncEnabledTrue();
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.syncEnabled = true")
    List<User> findActiveUsersWithSyncEnabled();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
}