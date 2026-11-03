// Records a game's start and final score, and gets a player's profile stats.
package com.anyguessr.anyguessrbackend.stats.application;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import java.time.Clock;
import java.util.List;

public class StatsService {

    public record UserStats(
            PlayerTotals totals,
            List<FinishedGame> recentGames
    ) {
    }

    private static final int RECENT_GAME_COUNT = 10;

    private final GameRecordRepository gameRecordRepository;
    private final Clock clock;

    public StatsService(GameRecordRepository gameRecordRepository, Clock clock) {
        if (gameRecordRepository == null || clock == null) {
            throw new IllegalArgumentException("Game record repository and clock must not be null");
        }

        this.gameRecordRepository = gameRecordRepository;
        this.clock = clock;
    }

    public void startGame(GameSessionId sessionId, UserId userId, MapId mapId) {
        gameRecordRepository.add(GameRecord.start(sessionId, userId, mapId, clock.instant()));
    }

    public void finishGame(GameSessionId sessionId, Score score) {
        gameRecordRepository.findBySessionId(sessionId).ifPresent(gameRecord -> {
            gameRecord.finish(clock.instant(), score);
            gameRecordRepository.markFinished(gameRecord);
        });
    }

    public UserStats getUserStats(UserId userId) {
        return new UserStats(
                gameRecordRepository.findTotalsForUser(userId),
                gameRecordRepository.findRecentFinishedGames(userId, RECENT_GAME_COUNT)
        );
    }
}
