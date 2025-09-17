package com.goldenbridge.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.goldenbridge.app.entity.GarminCredentials;
import com.goldenbridge.app.entity.User;

@Repository
public interface GarminCredentialsRepository extends JpaRepository<GarminCredentials, Long> {
    
    Optional<GarminCredentials> findByUser(User user);
    
    Optional<GarminCredentials> findByUserId(Long userId);
    
    List<GarminCredentials> findByIsValidTrue();
    
    List<GarminCredentials> findByIsValidFalse();
    
    @Query("SELECT gc FROM GarminCredentials gc WHERE gc.isValid = true AND gc.lastValidatedAt < :cutoffTime")
    List<GarminCredentials> findCredentialsNeedingValidation(LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(gc) FROM GarminCredentials gc WHERE gc.isValid = true")
    Long countValidCredentials();
}