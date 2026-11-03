package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_sessions")
public class QuizSessionEntity {

    @Id
    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "map_id", nullable = false)
    private Long mapId;

    @Column(name = "current_target_region_id", nullable = false)
    private Long currentTargetRegionId;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "answered_targets", nullable = false)
    private int answeredTargets;

    @Column(name = "correction_required", nullable = false)
    private boolean correctionRequired;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    protected QuizSessionEntity() {
    }

    public QuizSessionEntity(
            String sessionId,
            Long mapId,
            Long currentTargetRegionId,
            int correctAnswers,
            int answeredTargets,
            boolean correctionRequired,
            String status
    ) {
        this.sessionId = sessionId;
        this.mapId = mapId;
        this.currentTargetRegionId = currentTargetRegionId;
        this.correctAnswers = correctAnswers;
        this.answeredTargets = answeredTargets;
        this.correctionRequired = correctionRequired;
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getMapId() {
        return mapId;
    }

    public Long getCurrentTargetRegionId() {
        return currentTargetRegionId;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getAnsweredTargets() {
        return answeredTargets;
    }

    public boolean isCorrectionRequired() {
        return correctionRequired;
    }

    public String getStatus() {
        return status;
    }
}