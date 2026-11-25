package com.anyguessr.anyguessrbackend.stats.application;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.EarnedTrophy;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FirstGuess;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.LeaderboardEntry;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.MapBest;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.OverallRank;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapBadge;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.PlayStreak;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.Trophy;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.domain.WeakRegion;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Records stats games and builds the profile, map, result, comparison and leaderboard views. */
public class StatsService {

    /** A trophy and how close the player is to it. */
    public record TrophyStatus(Trophy trophy, Instant awardedAt, int progress) {}

    /** A map badge and whether the player has earned it. */
    public record BadgeStatus(MapBadge badge, boolean earned) {}

    /** Everything shown on a player's profile stats. */
    public record UserStats(
            List<TrophyStatus> trophies,
            List<MapBest> bestPerMap,
            PlayStreak streak,
            OverallRank overallRank
    ) {
    }

    /** A player's stats on one map. */
    public record MapDetail(
            PlayerTotals totals,
            MapBest best,
            List<FinishedGame> recentGames,
            List<WeakRegion> weakRegions,
            List<BadgeStatus> badges
    ) {
    }

    /** The result screen for one finished game. */
    public record GameResult(
            GameRecord game,
            GameRecord previousBest,
            MapBest mapBest,
            PlayStreak streak,
            Set<Trophy> newTrophies,
            List<String> missedRegions
    ) {

        /** Returns {@code true} if this game beat the previous best on the map, or there was none. */
        public boolean isNewBest() {
            return previousBest == null || game.isBetterThan(previousBest);
        }
    }

    /** Two players' best games on the same map. */
    public record MapComparison(MapBest yours, MapBest theirs) {

        /** Returns whichever best is present, used for the map's ID and name. */
        public MapBest either() {
            return yours != null ? yours : theirs;
        }
    }

    private static final int MAP_GAME_COUNT = 20;
    private static final int LEADERBOARD_SIZE = 100;

    private final GameRecordRepository gameRecordRepository;
    private final Clock clock;

    /** Creates the service. */
    public StatsService(GameRecordRepository gameRecordRepository, Clock clock) {
        if (gameRecordRepository == null || clock == null) {
            throw new IllegalArgumentException("Game record repository and clock must not be null");
        }

        this.gameRecordRepository = gameRecordRepository;
        this.clock = clock;
    }

    /** Records the start of a game. */
    public void startGame(GameSessionId sessionId, UserId userId, MapId mapId) {
        gameRecordRepository.add(GameRecord.start(sessionId, userId, mapId, clock.instant()));
    }

    /** Records a guess made in a game. */
    public void recordGuess(GameSessionId sessionId, GameRecord.Guess guess, Instant guessedAt) {
        gameRecordRepository.addGuess(sessionId, guess, guessedAt);
    }

    /** Finishes a game, then records any new best and trophies; does nothing if it never started. */
    public void finishGame(GameSessionId sessionId, Score score, Instant finishedAt) {
        gameRecordRepository.findBySessionId(sessionId).ifPresent(gameRecord -> {
            gameRecord.finish(finishedAt, score);
            gameRecordRepository.markFinished(gameRecord);
            recordIfBest(gameRecord);
            awardTrophies(gameRecord, finishedAt);
        });
    }

    /** Returns the player's profile stats. */
    public UserStats getUserStats(UserId userId) {
        Instant now = clock.instant();
        List<GameRecord> games = gameRecordRepository.findFinishedGames(userId);

        return new UserStats(
                trophyStatuses(userId, Trophy.Facts.from(games, now)),
                gameRecordRepository.findBestPerMap(userId),
                PlayStreak.from(games, now),
                gameRecordRepository.findOverallRank(userId).orElse(null)
        );
    }

    /** Returns the player's stats on the map. */
    public MapDetail getMapDetail(UserId userId, MapId mapId) {
        PlayerTotals totals = gameRecordRepository.findTotalsOnMap(userId, mapId);
        MapBest best = bestByMap(userId).get(mapId);

        return new MapDetail(
                totals,
                best,
                gameRecordRepository.findRecentFinishedGamesOnMap(userId, mapId, MAP_GAME_COUNT),
                WeakRegion.rank(gameRecordRepository.findRecentFirstGuesses(userId, mapId, MAP_GAME_COUNT)),
                Arrays.stream(MapBadge.values())
                        .map(badge -> new BadgeStatus(badge, badge.isEarned(totals.gamesPlayed(), best)))
                        .toList()
        );
    }

    /** Returns the game's result, or empty if the game is unknown or not finished. */
    public Optional<GameResult> getGameResult(GameSessionId sessionId) {
        return gameRecordRepository.findBySessionId(sessionId)
                .filter(GameRecord::isFinished)
                .map(this::resultOf);
    }

    /** Compares two players' best games on every map either of them has played. */
    public List<MapComparison> compare(UserId userId, UserId otherId) {
        Map<MapId, MapBest> yours = bestByMap(userId);
        Map<MapId, MapBest> theirs = bestByMap(otherId);

        return Stream.concat(yours.keySet().stream(), theirs.keySet().stream())
                .distinct()
                .map(mapId -> new MapComparison(yours.get(mapId), theirs.get(mapId)))
                .sorted(Comparator.comparing(comparison -> comparison.either().mapName()))
                .toList();
    }

    /** Returns the top 100 players on the map, best first. */
    public List<LeaderboardEntry> getLeaderboard(MapId mapId) {
        return gameRecordRepository.findLeaderboard(mapId, LEADERBOARD_SIZE);
    }

    /** Deletes all of a player's stats. */
    public void deleteUserStats(UserId userId) {
        gameRecordRepository.deleteAllForUser(userId);
    }

    private List<TrophyStatus> trophyStatuses(UserId userId, Trophy.Facts facts) {
        Map<Trophy, Instant> earned = gameRecordRepository.findTrophies(userId).stream()
                .collect(Collectors.toMap(EarnedTrophy::trophy, EarnedTrophy::awardedAt));

        return Arrays.stream(Trophy.values())
                .map(trophy -> new TrophyStatus(trophy, earned.get(trophy), trophy.progress(facts)))
                .toList();
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

    /** Returns how many games the player has finished. */
    public int getGamesPlayed(UserId userId) {
        return gameRecordRepository.findFinishedGames(userId).size();
    }
}
