package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_session_targets")
public class QuizSessionTargetEntity {

    @EmbeddedId
    private QuizSessionTargetId id;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    protected QuizSessionTargetEntity() {
    }

    public QuizSessionTargetEntity(
            String sessionId,
            int targetPosition,
            Long regionId,
            boolean completed
    ) {
        this.id = new QuizSessionTargetId(
                sessionId,
                targetPosition
        );

        this.regionId = regionId;
        this.completed = completed;
    }

    public QuizSessionTargetId getId() {
        return id;
    }

    public Long getRegionId() {
        return regionId;
    }

    public boolean isCompleted() {
        return completed;
    }
}