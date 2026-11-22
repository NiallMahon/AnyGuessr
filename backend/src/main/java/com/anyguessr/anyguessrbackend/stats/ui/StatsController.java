// REST endpoints for a player's profile stats, map detail, game results, comparisons, map leaderboards and deleting stats.
package com.anyguessr.anyguessrbackend.stats.ui;

import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.BadgeStatus;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.GameResult;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.MapComparison;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.MapDetail;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.TrophyStatus;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.UserStats;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.LeaderboardEntry;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.MapBest;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.OverallRank;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.Trophy;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.domain.WeakRegion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * REST controller under {@code /api/stats} for profile stats, map detail, game
 * results, player comparisons, map leaderboards and deleting stats.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    /**
     * JSON for one recent game.
     *
     * @param sessionId       the game
     * @param mapName         name of the map played
     * @param finishedAt      when the game finished
     * @param correct         targets right on the first try
     * @param incorrect       targets wrong on the first try
     * @param accuracyPercent percentage right on the first try
     * @param durationSeconds how long the game took
     */
    public record RecentGameResponse(
            String sessionId,
            String mapName,
            Instant finishedAt,
            int correct,
            int incorrect,
            double accuracyPercent,
            int durationSeconds
    ) {

        /**
         * Returns the game as JSON.
         *
         * @param game the finished game
         * @return the game as JSON
         */
        public static RecentGameResponse from(FinishedGame game) {
            return new RecentGameResponse(
                    game.sessionId().value(),
                    game.mapName(),
                    game.finishedAt(),
                    game.score().correct(),
                    game.score().incorrect(),
                    game.score().accuracyPercent(),
                    game.durationSeconds()
            );
        }
    }

    /**
     * JSON for one trophy.
     *
     * @param type        the trophy's enum name
     * @param title       the trophy name
     * @param description how to earn it
     * @param awardedAt   when it was earned, or {@code null}
     * @param progress    progress toward the goal
     * @param goal        the number needed to earn it
     */
    public record TrophyResponse(
            String type,
            String title,
            String description,
            Instant awardedAt,
            int progress,
            int goal
    ) {

        /**
         * Returns the trophy as JSON.
         *
         * @param status the trophy and the player's progress
         * @return the trophy as JSON
         */
        public static TrophyResponse from(TrophyStatus status) {
            Trophy trophy = status.trophy();

            return new TrophyResponse(
                    trophy.name(),
                    trophy.getTitle(),
                    trophy.getDescription(),
                    status.awardedAt(),
                    status.progress(),
                    trophy.getGoal()
            );
        }
    }

    /**
     * JSON for a player's best game on a map.
     *
     * @param mapId           the map
     * @param mapName         name of the map
     * @param accuracyPercent accuracy of the best game
     * @param flawless        whether the best game was flawless
     * @param durationSeconds length of the best game
     * @param place           leaderboard place
     * @param players         number of players on the leaderboard
     */
    public record MapBestResponse(
            long mapId,
            String mapName,
            double accuracyPercent,
            boolean flawless,
            int durationSeconds,
            int place,
            int players
    ) {

        /**
         * Returns the best game as JSON.
         *
         * @param best the best game on the map
         * @return the best game as JSON
         */
        public static MapBestResponse from(MapBest best) {
            return new MapBestResponse(
                    best.mapId().value(),
                    best.mapName(),
                    best.score().accuracyPercent(),
                    Trophy.isFlawless(best.score()),
                    best.durationSeconds(),
                    best.place(),
                    best.players()
            );
        }
    }

    /**
     * JSON for one leaderboard row.
     *
     * @param place           leaderboard place
     * @param userId          the player
     * @param username        the player's name
     * @param accuracyPercent accuracy of their best game
     * @param durationSeconds length of their best game
     */
    public record LeaderboardEntryResponse(
            int place,
            long userId,
            String username,
            double accuracyPercent,
            int durationSeconds
    ) {

        /**
         * Returns the row as JSON.
         *
         * @param entry the leaderboard row
         * @return the row as JSON
         */
        public static LeaderboardEntryResponse from(LeaderboardEntry entry) {
            return new LeaderboardEntryResponse(
                    entry.place(),
                    entry.userId().value(),
                    entry.username(),
                    entry.accuracyPercent(),
                    entry.durationSeconds()
            );
        }
    }

    /**
     * JSON for a player's profile stats.
     *
     * @param currentStreakDays the streak running now
     * @param bestStreakDays    the longest streak ever
     * @param trophies          every trophy with progress
     * @param bestPerMap        the best game on each map
     * @param overallPlace      overall place, or {@code null} if unranked
     * @param overallPlayers    number of ranked players, or {@code null} if unranked
     */
    public record UserStatsResponse(
            int currentStreakDays,
            int bestStreakDays,
            List<TrophyResponse> trophies,
            List<MapBestResponse> bestPerMap,
            Integer overallPlace,
            Integer overallPlayers
    ) {

        /**
         * Returns the stats as JSON.
         *
         * @param stats the player's profile stats
         * @return the stats as JSON
         */
        public static UserStatsResponse from(UserStats stats) {
            OverallRank overall = stats.overallRank();

            return new UserStatsResponse(
                    stats.streak().currentDays(),
                    stats.streak().bestDays(),
                    stats.trophies().stream().map(TrophyResponse::from).toList(),
                    stats.bestPerMap().stream().map(MapBestResponse::from).toList(),
                    overall == null ? null : overall.place(),
                    overall == null ? null : overall.players()
            );
        }
    }

    /**
     * JSON for a region the player keeps getting wrong.
     *
     * @param name        name of the region
     * @param attempts    times it was asked for
     * @param correct     times it was right on the first try
     * @param mixedUpWith region most often picked instead, or {@code null}
     */
    public record WeakRegionResponse(String name, int attempts, int correct, String mixedUpWith) {

        /**
         * Returns the region as JSON.
         *
         * @param region the weak region
         * @return the region as JSON
         */
        public static WeakRegionResponse from(WeakRegion region) {
            return new WeakRegionResponse(region.name(), region.attempts(), region.correct(), region.mixedUpWith());
        }
    }

    /**
     * JSON for one map badge.
     *
     * @param title       the badge name
     * @param description how to earn it
     * @param earned      whether the player has earned it
     */
    public record MapBadgeResponse(String title, String description, boolean earned) {

        /**
         * Returns the badge as JSON.
         *
         * @param status the badge and whether it is earned
         * @return the badge as JSON
         */
        public static MapBadgeResponse from(BadgeStatus status) {
            return new MapBadgeResponse(status.badge().getTitle(), status.badge().getDescription(), status.earned());
        }
    }

    /**
     * JSON for a player's stats on one map.
     *
     * @param gamesPlayed        finished games on the map
     * @param accuracyPercent    accuracy across all games on the map
     * @param bestPaceSeconds    fastest seconds per target, or {@code null}
     * @param averagePaceSeconds average seconds per target, or {@code null}
     * @param best               the best game, or {@code null}
     * @param recentGames        the latest finished games
     * @param weakRegions        regions the player keeps getting wrong
     * @param badges             every map badge and whether it is earned
     */
    public record MapDetailResponse(
            int gamesPlayed,
            double accuracyPercent,
            Double bestPaceSeconds,
            Double averagePaceSeconds,
            MapBestResponse best,
            List<RecentGameResponse> recentGames,
            List<WeakRegionResponse> weakRegions,
            List<MapBadgeResponse> badges
    ) {

        /**
         * Returns the stats as JSON.
         *
         * @param detail the player's stats on the map
         * @return the stats as JSON
         */
        public static MapDetailResponse from(MapDetail detail) {
            PlayerTotals totals = detail.totals();

            return new MapDetailResponse(
                    totals.gamesPlayed(),
                    totals.score().accuracyPercent(),
                    totals.bestPaceSeconds(),
                    totals.averagePaceSeconds(),
                    detail.best() == null ? null : MapBestResponse.from(detail.best()),
                    detail.recentGames().stream().map(RecentGameResponse::from).toList(),
                    detail.weakRegions().stream().map(WeakRegionResponse::from).toList(),
                    detail.badges().stream().map(MapBadgeResponse::from).toList()
            );
        }
    }

    /**
     * JSON for the result screen of one finished game.
     *
     * @param mapId               the map played
     * @param correct             targets right on the first try
     * @param incorrect           targets wrong on the first try
     * @param accuracyPercent     percentage right on the first try
     * @param durationSeconds     how long the game took
     * @param newBest             whether this is the player's new best on the map
     * @param previousBestPercent accuracy of the previous best, or {@code null}
     * @param previousBestSeconds length of the previous best, or {@code null}
     * @param place               leaderboard place of the current best, or {@code null}
     * @param players             number of players on the leaderboard, or {@code null}
     * @param streakDays          the player's current streak
     * @param newTrophies         names of trophies this game earned
     * @param missedRegions       targets missed on the first try
     */
    public record GameResultResponse(
            long mapId,
            int correct,
            int incorrect,
            double accuracyPercent,
            int durationSeconds,
            boolean newBest,
            Double previousBestPercent,
            Integer previousBestSeconds,
            Integer place,
            Integer players,
            int streakDays,
            List<String> newTrophies,
            List<String> missedRegions
    ) {

        /**
         * Returns the result as JSON.
         *
         * @param result the game result
         * @return the result as JSON
         */
        public static GameResultResponse from(GameResult result) {
            GameRecord game = result.game();
            GameRecord previous = result.previousBest();
            MapBest best = result.mapBest();

            return new GameResultResponse(
                    game.getMapId().value(),
                    game.getScore().correct(),
                    game.getScore().incorrect(),
                    game.getScore().accuracyPercent(),
                    game.durationSeconds(),
                    result.isNewBest(),
                    previous == null ? null : previous.getScore().accuracyPercent(),
                    previous == null ? null : previous.durationSeconds(),
                    best == null ? null : best.place(),
                    best == null ? null : best.players(),
                    result.streak().currentDays(),
                    result.newTrophies().stream().map(Trophy::getTitle).toList(),
                    result.missedRegions()
            );
        }
    }

    /**
     * JSON for two players' best games on one map.
     *
     * @param mapId   the map
     * @param mapName name of the map
     * @param yours   the first player's best, or {@code null}
     * @param theirs  the other player's best, or {@code null}
     */
    public record MapComparisonResponse(long mapId, String mapName, MapBestResponse yours, MapBestResponse theirs) {

        /**
         * Returns the comparison as JSON.
         *
         * @param comparison the two players' bests on the map
         * @return the comparison as JSON
         */
        public static MapComparisonResponse from(MapComparison comparison) {
            MapBest either = comparison.either();

            return new MapComparisonResponse(
                    either.mapId().value(),
                    either.mapName(),
                    comparison.yours() == null ? null : MapBestResponse.from(comparison.yours()),
                    comparison.theirs() == null ? null : MapBestResponse.from(comparison.theirs())
            );
        }
    }

    private final StatsService statsService;

    /**
     * Creates the controller.
     *
     * @param statsService the stats use cases
     */
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * {@code GET /api/stats/users/{userId}}: a player's profile stats.
     *
     * @param userId the player
     * @return the player's profile stats
     */
    @GetMapping("/users/{userId}")
    public UserStatsResponse getUserStats(@PathVariable long userId) {
        return UserStatsResponse.from(
                statsService.getUserStats(new UserId(userId))
        );
    }

    /**
     * {@code GET /api/stats/users/{userId}/maps/{mapId}}: a player's stats on one map.
     *
     * @param userId the player
     * @param mapId  the map
     * @return the player's stats on the map
     */
    @GetMapping("/users/{userId}/maps/{mapId}")
    public MapDetailResponse getMapDetail(@PathVariable long userId, @PathVariable long mapId) {
        return MapDetailResponse.from(
                statsService.getMapDetail(new UserId(userId), new MapId(mapId))
        );
    }

    /**
     * {@code GET /api/stats/users/{userId}/compare/{otherId}}: compares two players map by map.
     *
     * @param userId  the first player
     * @param otherId the other player
     * @return one comparison per map
     */
    @GetMapping("/users/{userId}/compare/{otherId}")
    public List<MapComparisonResponse> compare(@PathVariable long userId, @PathVariable long otherId) {
        return statsService.compare(new UserId(userId), new UserId(otherId)).stream()
                .map(MapComparisonResponse::from)
                .toList();
    }

    /**
     * {@code GET /api/stats/games/{sessionId}}: the result of a finished game.
     *
     * @param sessionId the quiz session
     * @return 200 with the result, or 404 if the game is unknown or not finished
     */
    @GetMapping("/games/{sessionId}")
    public ResponseEntity<GameResultResponse> getGameResult(@PathVariable String sessionId) {
        return statsService.getGameResult(new GameSessionId(sessionId))
                .map(GameResultResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /api/stats/maps/{mapId}/leaderboard}: a map's leaderboard.
     *
     * @param mapId the map
     * @return the top players, best first
     */
    @GetMapping("/maps/{mapId}/leaderboard")
    public List<LeaderboardEntryResponse> getLeaderboard(@PathVariable long mapId) {
        return statsService.getLeaderboard(new MapId(mapId)).stream()
                .map(LeaderboardEntryResponse::from)
                .toList();
    }

    /**
     * {@code DELETE /api/stats/users/{userId}}: deletes all of a player's stats.
     * Responds with 204 No Content.
     *
     * @param userId the player
     */
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserStats(@PathVariable long userId) {
        statsService.deleteUserStats(new UserId(userId));
    }

    /**
     * Turns invalid input, such as a non-positive ID, into a 400 Bad Request.
     *
     * @param e the validation error
     * @return the error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
