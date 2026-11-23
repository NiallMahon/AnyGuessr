package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Object representing one player's game on one map.
 * Created on game start and finished on game end.
 */
public class GameRecord {

    /**
     * A single guess made during a game.
     *
     * @param target     the region the player was asked to find
     * @param selected   the region the player clicked
     * @param correct    whether the selected region was the target
     * @param clickedLat latitude of the click, or {@code null} if not known
     * @param clickedLng longitude of the click, or {@code null} if not known
     */
    public record Guess(RegionId target, RegionId selected, boolean correct, Double clickedLat, Double clickedLng) {}

    /**
     * Thrown when trying to finish a game that already has a final score.
     */
    public static class GameAlreadyFinishedException extends RuntimeException {

        /**
         * Creates the exception for the given game.
         *
         * @param sessionId the game that is already finished
         */
        public GameAlreadyFinishedException(GameSessionId sessionId) {
            super("Game " + sessionId.value() + " is already finished");
        }
    }

    private final GameSessionId sessionId;
    private final UserId userId;
    private final MapId mapId;
    private final Instant startedAt;

    private Instant finishedAt;
    private Score score;

    private GameRecord(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt,
            Instant finishedAt,
            Score score
    ) {
        if (sessionId == null || userId == null || mapId == null || startedAt == null) {
            throw new IllegalArgumentException("Session, user, map and start time must not be null");
        }

        this.sessionId = sessionId;
        this.userId = userId;
        this.mapId = mapId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.score = score;
    }

    /**
     * Starts a new, unfinished game.
     *
     * @param sessionId the quiz session the game is played in
     * @param userId    the player
     * @param mapId     the map being played
     * @param startedAt when the game started
     * @return the new game
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public static GameRecord start(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt
    ) {
        return new GameRecord(sessionId, userId, mapId, startedAt, null, null);
    }

    /**
     * Rebuilds a game that was loaded from storage.
     *
     * @param sessionId  the quiz session the game was played in
     * @param userId     the player
     * @param mapId      the map that was played
     * @param startedAt  when the game started
     * @param finishedAt when the game finished, or {@code null} if it is still going
     * @param score      the final score, or {@code null} if it is still going
     * @return the restored game
     */
    public static GameRecord restore(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt,
            Instant finishedAt,
            Score score
    ) {
        return new GameRecord(sessionId, userId, mapId, startedAt, finishedAt, score);
    }

    /**
     * Finishes the game with its final score.
     *
     * @param finishedAt when the game finished
     * @param score      the final score
     * @throws GameAlreadyFinishedException if the game is already finished
     * @throws IllegalArgumentException     if an argument is {@code null} or the game would finish before it started
     */
    public void finish(Instant finishedAt, Score score) {
        if (isFinished()) {
            throw new GameAlreadyFinishedException(sessionId);
        }

        if (finishedAt == null || score == null) {
            throw new IllegalArgumentException("Finish time and score must not be null");
        }

        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("A game cannot finish before it started");
        }

        this.finishedAt = finishedAt;
        this.score = score;
    }

    /**
     * Returns how long the game took.
     *
     * @return the game length in seconds
     * @throws IllegalStateException if the game has not finished
     */
    public int durationSeconds() {
        requireFinished();
        return (int) Duration.between(startedAt, finishedAt).toSeconds();
    }

    /**
     * Returns the average time spent on each target.
     *
     * @return seconds per target
     * @throws IllegalStateException if the game has not finished
     */
    public double secondsPerTarget() {
        return durationSeconds() / (double) score.total();
    }

    /**
     * Compares this game with another: higher accuracy wins, and a faster time
     * breaks a tie.
     *
     * @param other the game to compare against
     * @return {@code true} if this game is better
     * @throws IllegalStateException if either game has not finished
     */
    public boolean isBetterThan(GameRecord other) {
        if (!isFinished() || !other.isFinished()) {
            throw new IllegalStateException("Only finished games can be compared");
        }

        int byAccuracy = Double.compare(score.accuracyPercent(), other.score.accuracyPercent());
        return byAccuracy != 0 ? byAccuracy > 0 : durationSeconds() < other.durationSeconds();
    }

    /**
     * Returns {@code true} if the game has a final score.
     *
     * @return {@code true} if the game has a final score
     */
    public boolean isFinished() {
        return finishedAt != null;
    }

    /**
     * Returns the quiz session the game was played in.
     *
     * @return the quiz session the game was played in
     */
    public GameSessionId getSessionId() {
        return sessionId;
    }

    /**
     * Returns the player.
     *
     * @return the player
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * Returns the map that was played.
     *
     * @return the map that was played
     */
    public MapId getMapId() {
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
     * Returns when the game finished.
     *
     * @return when the game finished
     * @throws IllegalStateException if the game has not finished
     */
    public Instant getFinishedAt() {
        requireFinished();
        return finishedAt;
    }

    /**
     * Returns the final score.
     *
     * @return the final score
     * @throws IllegalStateException if the game has not finished
     */
    public Score getScore() {
        requireFinished();
        return score;
    }

    private void requireFinished() {
        if (!isFinished()) {
            throw new IllegalStateException("Game has not finished yet");
        }
    }
}
