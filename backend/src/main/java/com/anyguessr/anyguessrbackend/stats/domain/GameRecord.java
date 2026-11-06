// One player's game on a map, from its start to its final score.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Duration;
import java.time.Instant;

public class GameRecord {

    public record Guess(
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
    }

    public static class GameAlreadyFinishedException extends RuntimeException {

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

        if (finishedAt != null && finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("A game cannot finish before it started");
        }

        if ((finishedAt == null) != (score == null)) {
            throw new IllegalArgumentException("A game has a score only once it has finished");
        }

        this.sessionId = sessionId;
        this.userId = userId;
        this.mapId = mapId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.score = score;
    }

    public static GameRecord start(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt
    ) {
        return new GameRecord(sessionId, userId, mapId, startedAt, null, null);
    }

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

    public int durationSeconds() {
        requireFinished();
        return (int) Duration.between(startedAt, finishedAt).toSeconds();
    }

    public double secondsPerTarget() {
        return durationSeconds() / (double) score.total();
    }

    public boolean isBetterThan(GameRecord other) {
        if (!isFinished() || !other.isFinished()) {
            throw new IllegalStateException("Only finished games can be compared");
        }

        int byAccuracy = Double.compare(score.accuracyPercent(), other.score.accuracyPercent());
        return byAccuracy != 0 ? byAccuracy > 0 : durationSeconds() < other.durationSeconds();
    }

    public boolean isFinished() {
        return finishedAt != null;
    }

    public GameSessionId getSessionId() {
        return sessionId;
    }

    public UserId getUserId() {
        return userId;
    }

    public MapId getMapId() {
        return mapId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        requireFinished();
        return finishedAt;
    }

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
