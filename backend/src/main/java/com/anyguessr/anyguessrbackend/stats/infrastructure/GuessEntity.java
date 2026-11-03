// One row of the guesses table; a guess is found by its game plus its attempt number.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "guesses")
@IdClass(GuessEntity.Key.class)
public class GuessEntity implements Persistable<GuessEntity.Key> {

    public record Key(Long gameId, Integer attemptNumber) implements Serializable {}

    @Id
    @Column(name = "game_id")
    private Long gameId;

    @Id
    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "map_id", nullable = false)
    private long mapId;

    @Column(name = "target_region_id", nullable = false)
    private long targetRegionId;

    @Column(name = "selected_region_id")
    private Long selectedRegionId;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "clicked_lat")
    private Double lat;

    @Column(name = "clicked_lng")
    private Double lng;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "guessed_at", nullable = false)
    private Instant guessedAt;

    protected GuessEntity() {
    }

    public GuessEntity(
            long gameId,
            int attemptNumber,
            long mapId,
            long targetRegionId,
            Long selectedRegionId,
            boolean correct,
            Double lat,
            Double lng,
            Instant guessedAt
    ) {
        this.gameId = gameId;
        this.attemptNumber = attemptNumber;
        this.mapId = mapId;
        this.targetRegionId = targetRegionId;
        this.selectedRegionId = selectedRegionId;
        this.correct = correct;
        this.lat = lat;
        this.lng = lng;
        this.guessedAt = guessedAt;
    }

    @Override
    public Key getId() {
        return new Key(gameId, attemptNumber);
    }

    // Guesses are only ever added, so Spring Data inserts them without looking them up first.
    @Override
    public boolean isNew() {
        return true;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public long getTargetRegionId() {
        return targetRegionId;
    }

    public Long getSelectedRegionId() {
        return selectedRegionId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Instant getGuessedAt() {
        return guessedAt;
    }
}
