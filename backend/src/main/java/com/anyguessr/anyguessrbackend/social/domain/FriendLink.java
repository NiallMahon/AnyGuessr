package com.anyguessr.anyguessrbackend.social.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "friends")
public class FriendLink {

    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    }

    public static class FriendLinkId implements Serializable {

        @Column(name = "user_id_a", nullable = false)
        private Long userIdA;

        @Column(name = "user_id_b", nullable = false)
        private Long userIdB;

        public FriendLinkId() {}

        public FriendLinkId(Long userIdA, Long userIdB) {
            this.userIdA = userIdA;
            this.userIdB = userIdB;
        }

        public Long getUserIdA() {
            return userIdA;
        }

        public void setUserIdA(Long userIdA) {
            this.userIdA = userIdA;
        }

        public Long getUserIdB() {
            return userIdB;
        }

        public void setUserIdB(Long userIdB) {
            this.userIdB = userIdB;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FriendLinkId that)) {
                return false;
            }
            return Objects.equals(userIdA, that.userIdA) && Objects.equals(userIdB, that.userIdB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userIdA, userIdB);
        }
    }

    @EmbeddedId
    private FriendLinkId id;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "status", length = 10, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FriendLink() {}

    public FriendLink(Long firstUserId, Long secondUserId, Long requestedBy, Status status) {
        long minId = Math.min(firstUserId, secondUserId);
        long maxId = Math.max(firstUserId, secondUserId);
        this.id = new FriendLinkId(minId, maxId);
        this.requestedBy = requestedBy;
        this.status = status.name();
        this.createdAt = Instant.now();
    }

    public FriendLinkId getId() {
        return id;
    }

    public void setId(FriendLinkId id) {
        this.id = id;
    }

    public Long getUserIdA() {
        return id != null ? id.getUserIdA() : null;
    }

    public Long getUserIdB() {
        return id != null ? id.getUserIdB() : null;
    }

    public Long getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Long requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}