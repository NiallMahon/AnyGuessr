// REST endpoint for a player's profile stats. Games are recorded from gameplay, never over HTTP.
package com.anyguessr.anyguessrbackend.stats.ui;

import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.UserStats;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public record UserStatsResponse(
            int gamesPlayed,
            int correctGuesses,
            int incorrectGuesses,
            double accuracyPercent,
            Integer bestTimeSeconds,
            Integer averageTimeSeconds,
            List<RecentGameResponse> recentGames
    ) {

        public static UserStatsResponse from(UserStats stats) {
            PlayerTotals totals = stats.totals();

            return new UserStatsResponse(
                    totals.gamesPlayed(),
                    totals.score().correct(),
                    totals.score().incorrect(),
                    totals.score().accuracyPercent(),
                    totals.bestTimeSeconds(),
                    totals.averageTimeSeconds(),
                    stats.recentGames().stream().map(RecentGameResponse::from).toList()
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
