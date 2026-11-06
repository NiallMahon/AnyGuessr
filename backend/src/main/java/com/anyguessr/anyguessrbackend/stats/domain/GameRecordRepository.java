// How games, guesses, best scores, trophies and profile stats are saved and looked up.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GameRecordRepository {

    record PlayerTotals(
            int gamesPlayed,
            Score score,
            Double bestPaceSeconds,
            Double averagePaceSeconds
    ) {
    }

    record FirstGuess(
            String target,
            String selected,
            boolean correct
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

    record EarnedTrophy(
            Trophy trophy,
            Instant awardedAt
    ) {
    }

    record MapBest(
            MapId mapId,
            String mapName,
            Score score,
            int durationSeconds,
            int place,
            int players
    ) {
    }

    record LeaderboardEntry(
            int place,
            UserId userId,
            String username,
            Score score,
            int durationSeconds
    ) {
    }

    Optional<GameRecord> findBySessionId(GameSessionId sessionId);

    void add(GameRecord gameRecord);

    void addGuess(
            GameSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng,
            Instant guessedAt
    );

    void markFinished(GameRecord gameRecord);

    PlayerTotals findTotalsForUser(UserId userId);

    PlayerTotals findTotalsOnMap(UserId userId, MapId mapId);

    List<FinishedGame> findRecentFinishedGames(UserId userId, int limit);

    List<FinishedGame> findRecentFinishedGamesOnMap(UserId userId, MapId mapId, int limit);

    List<GameRecord> findFinishedGames(UserId userId);

    List<FirstGuess> findFirstGuesses(GameSessionId sessionId);

    List<FirstGuess> findRecentFirstGuesses(UserId userId, MapId mapId, int games);

    Optional<GameRecord> findBestGame(UserId userId, MapId mapId);

    void saveBestGame(GameRecord gameRecord);

    List<EarnedTrophy> findTrophies(UserId userId);

    void addTrophies(UserId userId, Set<Trophy> trophies, Instant awardedAt);

    List<MapBest> findBestPerMap(UserId userId);

    List<LeaderboardEntry> findLeaderboard(MapId mapId, int limit);

    void deleteAllForUser(UserId userId);
}
