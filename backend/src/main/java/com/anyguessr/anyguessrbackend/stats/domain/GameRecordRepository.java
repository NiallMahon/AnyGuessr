// How games and profile stats are saved and looked up.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GameRecordRepository {

    record PlayerTotals(
            int gamesPlayed,
            Score score,
            Integer bestTimeSeconds,
            Integer averageTimeSeconds
    ) {
    }

    record FinishedGame(
            GameSessionId sessionId,
            String mapName,
            Instant finishedAt,
            Score score,
            int durationSeconds
    ) {
    }

    Optional<GameRecord> findBySessionId(GameSessionId sessionId);

    void add(GameRecord gameRecord);

    void markFinished(GameRecord gameRecord);

    PlayerTotals findTotalsForUser(UserId userId);

    List<FinishedGame> findRecentFinishedGames(UserId userId, int limit);
}
