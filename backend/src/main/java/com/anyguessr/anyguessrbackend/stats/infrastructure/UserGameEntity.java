// One row of the user_games table.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "user_games")
public class UserGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "map_id", nullable = false)
    private long mapId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "correct_guesses", nullable = false)
    private int correctGuesses;

    @Column(name = "incorrect_guesses", nullable = false)
    private int incorrectGuesses;

    protected UserGameEntity() {
    }

    public UserGameEntity(String sessionId, long userId, long mapId, Instant startedAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.mapId = mapId;
        this.startedAt = startedAt;
    }

    public Long getGameId() {
        return gameId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getUserId() {
        return userId;
    }

    public long getMapId() {
        return mapId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public int getCorrectGuesses() {
        return correctGuesses;
    }

    public int getIncorrectGuesses() {
        return incorrectGuesses;
    }
}
