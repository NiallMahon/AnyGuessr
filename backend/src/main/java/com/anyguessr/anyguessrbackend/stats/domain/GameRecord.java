// One player's game on a map; only the first click on each target is scored.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GameRecord {

    public record Guess(
            int attemptNumber,
            RegionId targetRegionId,
            RegionId selectedRegionId,
            double lat,
            double lng,
            Instant guessedAt
    ) {

        public Guess {
            if (attemptNumber < 1) {
                throw new IllegalArgumentException("Attempt number must be at least 1");
            }

            if (targetRegionId == null) {
                throw new IllegalArgumentException("Target region must not be null");
            }

            if (guessedAt == null) {
                throw new IllegalArgumentException("Guess time must not be null");
            }
        }

        public boolean correct() {
            return targetRegionId.equals(selectedRegionId);
        }
    }

    public record Summary(
            int correctGuesses,
            int incorrectGuesses,
            int durationSeconds,
            double accuracyPercent
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
    private final List<Guess> guesses;

    private Instant finishedAt;

    private GameRecord(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt,
            Instant finishedAt,
            List<Guess> guesses
    ) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Game session ID must not be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        if (mapId == null) {
            throw new IllegalArgumentException("Map ID must not be null");
        }

        if (startedAt == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }

        if (finishedAt != null && finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("A game cannot finish before it started");
        }

        if (guesses == null) {
            throw new IllegalArgumentException("Guesses must not be null");
        }

        for (int i = 0; i < guesses.size(); i++) {
            if (guesses.get(i).attemptNumber() != i + 1) {
                throw new IllegalArgumentException("Guesses must be numbered 1, 2, 3... in order");
            }
        }

        this.sessionId = sessionId;
        this.userId = userId;
        this.mapId = mapId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.guesses = new ArrayList<>(guesses);
    }

    public static GameRecord start(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt
    ) {
        return new GameRecord(sessionId, userId, mapId, startedAt, null, List.of());
    }

    public static GameRecord restore(
            GameSessionId sessionId,
            UserId userId,
            MapId mapId,
            Instant startedAt,
            Instant finishedAt,
            List<Guess> guesses
    ) {
        return new GameRecord(sessionId, userId, mapId, startedAt, finishedAt, guesses);
    }

    public Guess recordGuess(
            RegionId targetRegionId,
            RegionId selectedRegionId,
            double lat,
            double lng,
            Instant guessedAt
    ) {
        if (isFinished()) {
            throw new GameAlreadyFinishedException(sessionId);
        }

        if (guessedAt != null && guessedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("A guess cannot be made before the game started");
        }

        Guess guess = new Guess(
                guesses.size() + 1,
                targetRegionId,
                selectedRegionId,
                lat,
                lng,
                guessedAt
        );

        guesses.add(guess);

        return guess;
    }

    public Summary finish(Instant finishedAt) {
        if (isFinished()) {
            throw new GameAlreadyFinishedException(sessionId);
        }

        if (finishedAt == null) {
            throw new IllegalArgumentException("Finish time must not be null");
        }

        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("A game cannot finish before it started");
        }

        this.finishedAt = finishedAt;

        return summary();
    }

    public Summary summary() {
        if (!isFinished()) {
            throw new IllegalStateException("Game has not finished yet");
        }

        Set<RegionId> answeredTargets = new HashSet<>();
        int correct = 0;
        int incorrect = 0;

        for (Guess guess : guesses) {
            if (guess.selectedRegionId() == null || !answeredTargets.add(guess.targetRegionId())) {
                continue;
            }

            if (guess.correct()) {
                correct++;
            } else {
                incorrect++;
            }
        }

        int answered = correct + incorrect;
        int durationSeconds = (int) Duration.between(startedAt, finishedAt).toSeconds();
        double accuracyPercent = answered == 0 ? 0.0 : correct * 100.0 / answered;

        return new Summary(correct, incorrect, durationSeconds, accuracyPercent);
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

    public Optional<Instant> getFinishedAt() {
        return Optional.ofNullable(finishedAt);
    }

    public List<Guess> getGuesses() {
        return List.copyOf(guesses);
    }
}
