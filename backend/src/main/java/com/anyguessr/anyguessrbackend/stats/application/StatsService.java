// Records a game's start, guesses and finish, and gets a player's profile stats.
package com.anyguessr.anyguessrbackend.stats.application;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Guess;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Summary;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import java.time.Clock;
import java.util.List;

public class StatsService {

    public record GuessDetails(
            RegionId targetRegionId,
            RegionId selectedRegionId,
            boolean correct,
            Double lat,
            Double lng
    ) {
    }

    public record UserStats(
            PlayerTotals totals,
            List<FinishedGame> recentGames
    ) {
    }

    public static class GameNotFoundException extends RuntimeException {

        public GameNotFoundException(GameSessionId sessionId) {
            super("No game found for session " + sessionId.value());
        }
    }

    public static class GameAlreadyStartedException extends RuntimeException {

        public GameAlreadyStartedException(GameSessionId sessionId) {
            super("A game already exists for session " + sessionId.value());
        }
    }

    private static final int RECENT_GAME_COUNT = 3;

    private final GameRecordRepository gameRecordRepository;
    private final Clock clock;

    public StatsService(GameRecordRepository gameRecordRepository, Clock clock) {
        if (gameRecordRepository == null) {
            throw new IllegalArgumentException("Game record repository must not be null");
        }

        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }

        this.gameRecordRepository = gameRecordRepository;
        this.clock = clock;
    }

    public void startGame(GameSessionId sessionId, UserId userId, MapId mapId) {
        if (gameRecordRepository.findBySessionId(sessionId).isPresent()) {
            throw new GameAlreadyStartedException(sessionId);
        }

        if (!gameRecordRepository.userExists(userId)) {
            throw new IllegalArgumentException("Unknown user " + userId.value());
        }

        if (!gameRecordRepository.mapExists(mapId)) {
            throw new IllegalArgumentException("Unknown map " + mapId.value());
        }

        GameRecord gameRecord = GameRecord.start(sessionId, userId, mapId, clock.instant());

        gameRecordRepository.add(gameRecord);
    }

    public Guess recordGuess(GameSessionId sessionId, GuessDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("Guess details must not be null");
        }

        GameRecord gameRecord = findGame(sessionId);

        checkRegionIsOnMap(details.targetRegionId(), gameRecord.getMapId());

        if (details.selectedRegionId() != null) {
            checkRegionIsOnMap(details.selectedRegionId(), gameRecord.getMapId());
        }

        Guess guess = gameRecord.recordGuess(
                details.targetRegionId(),
                details.selectedRegionId(),
                details.correct(),
                details.lat(),
                details.lng(),
                clock.instant()
        );

        gameRecordRepository.addGuess(gameRecord, guess);

        return guess;
    }

    public Summary finishGame(GameSessionId sessionId) {
        GameRecord gameRecord = findGame(sessionId);

        Summary summary = gameRecord.finish(clock.instant());

        gameRecordRepository.markFinished(gameRecord);

        return summary;
    }

    public UserStats getUserStats(UserId userId) {
        return new UserStats(
                gameRecordRepository.findTotalsForUser(userId),
                gameRecordRepository.findRecentFinishedGames(userId, RECENT_GAME_COUNT)
        );
    }

    private GameRecord findGame(GameSessionId sessionId) {
        return gameRecordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new GameNotFoundException(sessionId));
    }

    private void checkRegionIsOnMap(RegionId regionId, MapId mapId) {
        if (!gameRecordRepository.regionIsOnMap(regionId, mapId)) {
            throw new IllegalArgumentException(
                    "Region " + regionId.value() + " is not on map " + mapId.value());
        }
    }
}
