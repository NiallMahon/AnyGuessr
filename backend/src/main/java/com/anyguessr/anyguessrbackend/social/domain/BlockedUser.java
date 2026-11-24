package com.anyguessr.anyguessrbackend.social.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "blocked_users")
public class BlockedUser {

    public static class BlockedUserId implements Serializable {

        @Column(name = "blocker_id", nullable = false)
        private Long blockerId;

        @Column(name = "blocked_id", nullable = false)
        private Long blockedId;

        public BlockedUserId() {}

        public BlockedUserId(Long blockerId, Long blockedId) {
            this.blockerId = blockerId;
            this.blockedId = blockedId;
        }

        public Long getBlockerId() {
            return blockerId;
        }

        public void setBlockerId(Long blockerId) {
            this.blockerId = blockerId;
        }

        public Long getBlockedId() {
            return blockedId;
        }

        public void setBlockedId(Long blockedId) {
            this.blockedId = blockedId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BlockedUserId that)) {
                return false;
            }
            return Objects.equals(blockerId, that.blockerId) && Objects.equals(blockedId, that.blockedId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockerId, blockedId);
        }
    }

    @EmbeddedId
    private BlockedUserId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BlockedUser() {}

    public BlockedUser(Long blockerId, Long blockedId) {
        this.id = new BlockedUserId(blockerId, blockedId);
        this.createdAt = Instant.now();
    }

    public BlockedUserId getId() {
        return id;
    }

    public void setId(BlockedUserId id) {
        this.id = id;
    }

    public Long getBlockerId() {
        return id != null ? id.getBlockerId() : null;
    }

    public Long getBlockedId() {
        return id != null ? id.getBlockedId() : null;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}