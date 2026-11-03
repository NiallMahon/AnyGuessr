// How games and profile stats are saved and looked up.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GameRecordRepository {

    record PlayerTotals(
            int gamesPlayed,
            int correctGuesses,
            int incorrectGuesses,
            Integer bestTimeSeconds,
            Integer averageTimeSeconds
    ) {

        public double accuracyPercent() {
            int total = correctGuesses + incorrectGuesses;
            return total == 0 ? 0.0 : correctGuesses * 100.0 / total;
        }
    }

    record FinishedGame(
            GameSessionId sessionId,
            String mapName,
            Instant finishedAt,
            int correctGuesses,
            int incorrectGuesses,
            int durationSeconds
    ) {

        public double accuracyPercent() {
            int total = correctGuesses + incorrectGuesses;
            return total == 0 ? 0.0 : correctGuesses * 100.0 / total;
        }
    }

    Optional<GameRecord> findBySessionId(GameSessionId sessionId);

    boolean userExists(UserId userId);

    boolean mapExists(MapId mapId);

    boolean regionIsOnMap(RegionId regionId, MapId mapId);

    void add(GameRecord gameRecord);

    void addGuess(GameRecord gameRecord, GameRecord.Guess guess);

    void markFinished(GameRecord gameRecord);

    PlayerTotals findTotalsForUser(UserId userId);

    List<FinishedGame> findRecentFinishedGames(UserId userId, int limit);
}
