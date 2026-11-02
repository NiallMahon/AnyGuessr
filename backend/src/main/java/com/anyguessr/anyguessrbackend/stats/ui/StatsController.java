// REST endpoints for recording games and getting a player's stats.
package com.anyguessr.anyguessrbackend.stats.ui;

import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.GameAlreadyStartedException;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.GameNotFoundException;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.GuessDetails;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.UserStats;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.GameAlreadyFinishedException;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Summary;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FinishedGame;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.PlayerTotals;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    public record StartGameRequest(long userId, long mapId) {}

    public record RecordGuessRequest(long targetRegionId, Long selectedRegionId, double lat, double lng) {}

    public record GameSummaryResponse(int correct, int incorrect, int durationSeconds, double accuracyPercent) {

        public static GameSummaryResponse from(Summary summary) {
            return new GameSummaryResponse(
                    summary.correctGuesses(),
                    summary.incorrectGuesses(),
                    summary.durationSeconds(),
                    summary.accuracyPercent()
            );
        }
    }

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
                    game.correctGuesses(),
                    game.incorrectGuesses(),
                    game.accuracyPercent(),
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
                    totals.correctGuesses(),
                    totals.incorrectGuesses(),
                    totals.accuracyPercent(),
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

    @PostMapping("/games/{sessionId}")
    public void startGame(
            @PathVariable String sessionId,
            @RequestBody StartGameRequest request
    ) {
        statsService.startGame(
                new GameSessionId(sessionId),
                new UserId(request.userId()),
                new MapId(request.mapId())
        );
    }

    @PostMapping("/games/{sessionId}/guesses")
    public void recordGuess(
            @PathVariable String sessionId,
            @RequestBody RecordGuessRequest request
    ) {
        GuessDetails details = new GuessDetails(
                new RegionId(request.targetRegionId()),
                request.selectedRegionId() == null ? null : new RegionId(request.selectedRegionId()),
                request.lat(),
                request.lng()
        );

        statsService.recordGuess(new GameSessionId(sessionId), details);
    }

    @PostMapping("/games/{sessionId}/finish")
    public GameSummaryResponse finishGame(@PathVariable String sessionId) {
        return GameSummaryResponse.from(
                statsService.finishGame(new GameSessionId(sessionId))
        );
    }

    @GetMapping("/users/{userId}")
    public UserStatsResponse getUserStats(@PathVariable long userId) {
        return UserStatsResponse.from(
                statsService.getUserStats(new UserId(userId))
        );
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleNotFound(GameNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({GameAlreadyStartedException.class, GameAlreadyFinishedException.class})
    public ProblemDetail handleConflict(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleClash(DuplicateKeyException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Another request changed this game at the same time, try again");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
