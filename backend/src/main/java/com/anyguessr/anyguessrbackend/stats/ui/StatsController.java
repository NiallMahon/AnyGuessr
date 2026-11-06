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

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    public record RecentGameResponse(
            String sessionId,
            String mapName,
            Instant finishedAt,
            int correct,
            int incorrect,
            double accuracyPercent,
            int durationSeconds
    ) {

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

    public record TrophyResponse(
            String type,
            String title,
            String description,
            Instant awardedAt,
            int progress,
            int goal
    ) {

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

    public record MapBestResponse(
            long mapId,
            String mapName,
            double accuracyPercent,
            boolean flawless,
            int durationSeconds,
            int place,
            int players
    ) {

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

    public record LeaderboardEntryResponse(
            int place,
            long userId,
            String username,
            double accuracyPercent,
            int durationSeconds
    ) {

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

    public record UserStatsResponse(
            int currentStreakDays,
            int bestStreakDays,
            List<TrophyResponse> trophies,
            List<MapBestResponse> bestPerMap,
            Integer overallPlace,
            Integer overallPlayers
    ) {

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

    public record WeakRegionResponse(String name, int attempts, int correct, String mixedUpWith) {

        public static WeakRegionResponse from(WeakRegion region) {
            return new WeakRegionResponse(region.name(), region.attempts(), region.correct(), region.mixedUpWith());
        }
    }

    public record MapBadgeResponse(String title, String description, boolean earned) {

        public static MapBadgeResponse from(BadgeStatus status) {
            return new MapBadgeResponse(status.badge().getTitle(), status.badge().getDescription(), status.earned());
        }
    }

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

    public record MapComparisonResponse(long mapId, String mapName, MapBestResponse yours, MapBestResponse theirs) {

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

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/users/{userId}")
    public UserStatsResponse getUserStats(@PathVariable long userId) {
        return UserStatsResponse.from(
                statsService.getUserStats(new UserId(userId))
        );
    }

    @GetMapping("/users/{userId}/maps/{mapId}")
    public MapDetailResponse getMapDetail(@PathVariable long userId, @PathVariable long mapId) {
        return MapDetailResponse.from(
                statsService.getMapDetail(new UserId(userId), new MapId(mapId))
        );
    }

    @GetMapping("/users/{userId}/compare/{otherId}")
    public List<MapComparisonResponse> compare(@PathVariable long userId, @PathVariable long otherId) {
        return statsService.compare(new UserId(userId), new UserId(otherId)).stream()
                .map(MapComparisonResponse::from)
                .toList();
    }

    @GetMapping("/games/{sessionId}")
    public ResponseEntity<GameResultResponse> getGameResult(@PathVariable String sessionId) {
        return statsService.getGameResult(new GameSessionId(sessionId))
                .map(GameResultResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/maps/{mapId}/leaderboard")
    public List<LeaderboardEntryResponse> getLeaderboard(@PathVariable long mapId) {
        return statsService.getLeaderboard(new MapId(mapId)).stream()
                .map(LeaderboardEntryResponse::from)
                .toList();
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserStats(@PathVariable long userId) {
        statsService.deleteUserStats(new UserId(userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
