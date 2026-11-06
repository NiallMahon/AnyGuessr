// Records games, guesses, best scores and trophies, and gets, ranks, compares or deletes a player's stats.
package com.anyguessr.anyguessrbackend.stats.application;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.EarnedTrophy;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FirstGuess;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.LeaderboardEntry;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.MapBest;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.PlayStreak;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.Trophy;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.domain.WeakRegion;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatsService {

    public record UserStats(
            PlayerTotals totals,
            List<FinishedGame> recentGames,
            List<EarnedTrophy> trophies,
            List<MapBest> bestPerMap,
            PlayStreak streak,
            Trophy.Facts trophyFacts
    ) {
    }

    public record MapDetail(
            PlayerTotals totals,
            List<FinishedGame> recentGames,
            List<WeakRegion> weakRegions
    ) {
    }

    public record GameResult(
            GameRecord game,
            GameRecord previousBest,
            MapBest mapBest,
            PlayStreak streak,
            Set<Trophy> newTrophies,
            List<String> missedRegions
    ) {

        public boolean isNewBest() {
            return previousBest == null || game.isBetterThan(previousBest);
        }
    }

    public record MapComparison(
            MapBest yours,
            MapBest theirs
    ) {
    }

    private static final int RECENT_GAME_COUNT = 10;
    private static final int MAP_GAME_COUNT = 20;
    private static final int LEADERBOARD_SIZE = 100;

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

    public void recordGuess(
            GameSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
        gameRecordRepository.addGuess(sessionId, target, selected, correct, clickedLat, clickedLng, clock.instant());
    }

    public void finishGame(GameSessionId sessionId, Score score) {
        gameRecordRepository.findBySessionId(sessionId).ifPresent(gameRecord -> {
            Instant now = clock.instant();
            gameRecord.finish(now, score);
            gameRecordRepository.markFinished(gameRecord);
            recordIfBest(gameRecord);
            awardTrophies(gameRecord, now);
        });
    }

    public UserStats getUserStats(UserId userId) {
        Instant now = clock.instant();
        List<GameRecord> games = gameRecordRepository.findFinishedGames(userId);

        return new UserStats(
                gameRecordRepository.findTotalsForUser(userId),
                gameRecordRepository.findRecentFinishedGames(userId, RECENT_GAME_COUNT),
                gameRecordRepository.findTrophies(userId),
                gameRecordRepository.findBestPerMap(userId),
                PlayStreak.from(games, now),
                Trophy.Facts.from(games, now)
        );
    }

    public MapDetail getMapDetail(UserId userId, MapId mapId) {
        return new MapDetail(
                gameRecordRepository.findTotalsOnMap(userId, mapId),
                gameRecordRepository.findRecentFinishedGamesOnMap(userId, mapId, MAP_GAME_COUNT),
                WeakRegion.rank(gameRecordRepository.findRecentFirstGuesses(userId, mapId, MAP_GAME_COUNT))
        );
    }

    public Optional<GameResult> getGameResult(GameSessionId sessionId) {
        return gameRecordRepository.findBySessionId(sessionId)
                .filter(GameRecord::isFinished)
                .map(this::resultOf);
    }

    public List<MapComparison> compare(UserId userId, UserId otherId) {
        Map<MapId, MapBest> yours = bestByMap(userId);
        Map<MapId, MapBest> theirs = bestByMap(otherId);

        return Stream.concat(yours.keySet().stream(), theirs.keySet().stream())
                .distinct()
                .map(mapId -> new MapComparison(yours.get(mapId), theirs.get(mapId)))
                .toList();
    }

    public List<LeaderboardEntry> getLeaderboard(MapId mapId) {
        return gameRecordRepository.findLeaderboard(mapId, LEADERBOARD_SIZE);
    }

    public void deleteUserStats(UserId userId) {
        gameRecordRepository.deleteAllForUser(userId);
    }

    private GameResult resultOf(GameRecord game) {
        Instant now = clock.instant();
        List<GameRecord> games = gameRecordRepository.findFinishedGames(game.getUserId());
        List<GameRecord> before = games.stream()
                .takeWhile(other -> !other.getSessionId().equals(game.getSessionId()))
                .toList();
        GameRecord previousBest = before.stream()
                .filter(other -> other.getMapId().equals(game.getMapId()))
                .reduce((best, other) -> other.isBetterThan(best) ? other : best)
                .orElse(null);
        Set<Trophy> heldBefore = Trophy.newlyEarned(Trophy.Facts.from(before, now), Set.of());

        return new GameResult(
                game,
                previousBest,
                bestByMap(game.getUserId()).get(game.getMapId()),
                PlayStreak.from(games, now),
                Trophy.newlyEarned(Trophy.Facts.from(games.subList(0, before.size() + 1), now), heldBefore),
                gameRecordRepository.findFirstGuesses(game.getSessionId()).stream()
                        .filter(guess -> !guess.correct())
                        .map(FirstGuess::target)
                        .toList()
        );
    }

    private Map<MapId, MapBest> bestByMap(UserId userId) {
        return gameRecordRepository.findBestPerMap(userId).stream()
                .collect(Collectors.toMap(MapBest::mapId, Function.identity()));
    }

    private void recordIfBest(GameRecord gameRecord) {
        boolean best = gameRecordRepository.findBestGame(gameRecord.getUserId(), gameRecord.getMapId())
                .map(gameRecord::isBetterThan)
                .orElse(true);

        if (best) {
            gameRecordRepository.saveBestGame(gameRecord);
        }
    }

    private void awardTrophies(GameRecord gameRecord, Instant now) {
        UserId userId = gameRecord.getUserId();
        Set<Trophy> held = gameRecordRepository.findTrophies(userId).stream()
                .map(EarnedTrophy::trophy)
                .collect(Collectors.toSet());

        gameRecordRepository.addTrophies(
                userId,
                Trophy.newlyEarned(Trophy.Facts.from(gameRecordRepository.findFinishedGames(userId), now), held),
                now
        );
    }
}
