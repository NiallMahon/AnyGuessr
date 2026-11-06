package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import com.anyguessr.anyguessrbackend.identity.domain.User;
import java.time.LocalDateTime;
import com.anyguessr.anyguessrbackend.identity.domain.User;
import jakarta.persistence.Column;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected UserEntity() {
        
    }

    public UserEntity(
            String username,
            String email
    ) {
        this.username = username;
        this.email = email;
    }

    @PrePersist
    private void beforeInsert() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public User toDomain() {
    return new User(
            userId,
            username,
            email,
            passwordHash
    );
}

public static UserEntity fromDomain(User user) {
    UserEntity entity = new UserEntity();
    entity.userId = user.id();
    entity.username = user.username();
    entity.email = user.email();
    entity.passwordHash = user.passwordHash();
    return entity;
}

}

