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

/**
 * JPA entity for one row of the {@code user_games} table: a player's game on a map.
 */
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

    /**
     * Empty constructor required by JPA.
     */
    protected UserGameEntity() {
    }

    /**
     * Creates an unfinished game row.
     *
     * @param sessionId the quiz session
     * @param userId    the player
     * @param mapId     the map being played
     * @param startedAt when the game started
     */
    public UserGameEntity(String sessionId, long userId, long mapId, Instant startedAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.mapId = mapId;
        this.startedAt = startedAt;
    }

    /**
     * Returns the database ID, or {@code null} before the row is saved.
     *
     * @return the database ID, or {@code null} before the row is saved
     */
    public Long getGameId() {
        return gameId;
    }

    /**
     * Returns the quiz session.
     *
     * @return the quiz session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the player.
     *
     * @return the player
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Returns the map played.
     *
     * @return the map played
     */
    public long getMapId() {
        return mapId;
    }

    /**
     * Returns when the game started.
     *
     * @return when the game started
     */
    public Instant getStartedAt() {
        return startedAt;
    }

    /**
     * Returns when the game finished, or {@code null} if it has not.
     *
     * @return when the game finished, or {@code null} if it has not
     */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /**
     * Returns targets right on the first try.
     *
     * @return targets right on the first try
     */
    public int getCorrectGuesses() {
        return correctGuesses;
    }

    /**
     * Returns targets wrong on the first try.
     *
     * @return targets wrong on the first try
     */
    public int getIncorrectGuesses() {
        return incorrectGuesses;
    }
}
