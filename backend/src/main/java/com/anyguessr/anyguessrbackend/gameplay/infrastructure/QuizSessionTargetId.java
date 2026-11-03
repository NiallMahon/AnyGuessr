package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuizSessionTargetId implements Serializable {

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "target_position", nullable = false)
    private Integer targetPosition;

    protected QuizSessionTargetId() {
    }

    public QuizSessionTargetId(
            String sessionId,
            Integer targetPosition
    ) {
        this.sessionId = sessionId;
        this.targetPosition = targetPosition;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getTargetPosition() {
        return targetPosition;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof QuizSessionTargetId other)) {
            return false;
        }

        return Objects.equals(
                sessionId,
                other.sessionId
        ) && Objects.equals(
                targetPosition,
                other.targetPosition
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sessionId,
                targetPosition
        );
    }
}