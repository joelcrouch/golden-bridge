package com.goldenbridge.app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "garmin_credentials")
@EntityListeners(AuditingEntityListener.class)
public class GarminCredentials {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Garmin username is required")
    @Column(name = "garmin_username", nullable = false)
    private String garminUsername;
    
    @NotBlank(message = "Encrypted password is required")
    @Column(name = "encrypted_password", nullable = false, columnDefinition = "TEXT")
    private String encryptedPassword;
    
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = false;
    
    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;
    
    @Column(name = "validation_error")
    private String validationError;
    
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
    public GarminCredentials() {}
    
    public GarminCredentials(String garminUsername, String encryptedPassword, User user) {
        this.garminUsername = garminUsername;
        this.encryptedPassword = encryptedPassword;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getGarminUsername() { return garminUsername; }
    public void setGarminUsername(String garminUsername) { this.garminUsername = garminUsername; }
    
    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    
    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }
    
    public LocalDateTime getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(LocalDateTime lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }
    
    public String getValidationError() { return validationError; }
    public void setValidationError(String validationError) { this.validationError = validationError; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}