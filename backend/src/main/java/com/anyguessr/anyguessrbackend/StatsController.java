package com.anyguessr.anyguessrbackend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
import java.sql.Timestamp;

/*
Backend for game stats. frontend 'statsAPI.ts' calls this and records data per event and saves to db.

POST /api/stats/games                  records start timestamp, returns the new gameId
POST /api/stats/games/{gameId}/clicks  stores data per click
POST /api/stats/games/{gameId}/end     records end timestamp, calculates score, saves it

note that certain stats (correct/incorrect/accuracy/duration) are only calculated at game end.
 */

@RestController
@RequestMapping("/api/stats/games")
public class StatsController {

    public record StartRequest(long userId, long mapId) {}
    public record StartResponse(long gameId) {}
    public record ClickRequest(long targetRegionId, Long selectedRegionId, double lat, double lng, Instant clickedAt) {}
    public record GameEndResult(int correct, int incorrect, int durationSeconds, double accuracyPercent) {}

    private final JdbcTemplate jdbc;

    // Spring passes the JdbcTemplate in automatically
    public StatsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping
    public StartResponse recordGameStart(@RequestBody StartRequest req) {
        long gameId = new SimpleJdbcInsert(jdbc)
                .withTableName("user_games")
                .usingGeneratedKeyColumns("game_id")
                .executeAndReturnKey(Map.of(
                        "user_id", req.userId(),
                        "map_id", req.mapId(),
                        "started_at", LocalDateTime.now(),
                        "correct_guesses", 0,
                        "incorrect_guesses", 0))
                .longValue();
        return new StartResponse(gameId);
    }

    // stored as received, nothing calculated here
    @PostMapping("/{gameId}/clicks")
    public void recordRegionClick(@PathVariable long gameId, @RequestBody ClickRequest click) {
        Long mapId = jdbc.queryForObject(
                "SELECT map_id FROM user_games WHERE game_id = ? AND finished_at IS NULL",
                Long.class, gameId);
        Integer attempt = jdbc.queryForObject(
                "SELECT COALESCE(MAX(attempt_number), 0) + 1 FROM guesses WHERE game_id = ?",
                Integer.class, gameId);
        jdbc.update(
                "INSERT INTO guesses (game_id, attempt_number, map_id, target_region_id, "
                        + "selected_region_id, clicked_lat, clicked_lng, guessed_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                gameId, attempt, mapId, click.targetRegionId(), click.selectedRegionId(),
                click.lat(), click.lng(), Timestamp.from(click.clickedAt()));
    }

    // the only place anything is calculated
    @PostMapping("/{gameId}/end")
    public GameEndResult recordGameEnd(@PathVariable long gameId) {
        LocalDateTime startedAt = jdbc.queryForObject(
                "SELECT started_at FROM user_games WHERE game_id = ?",
                LocalDateTime.class, gameId);
        LocalDateTime now = LocalDateTime.now();

        int correct = 0;
        int incorrect = 0;
        for (Map<String, Object> g : jdbc.queryForList(
                "SELECT target_region_id, selected_region_id FROM guesses WHERE game_id = ?", gameId)) {
            if (Objects.equals(g.get("target_region_id"), g.get("selected_region_id"))) {
                correct++;
            } else {
                incorrect++;
            }
        }
        int durationSeconds = (int) Duration.between(startedAt, now).getSeconds();
        int total = correct + incorrect;
        double accuracyPercent = total == 0 ? 0 : correct * 100.0 / total;

        int updated = jdbc.update(
                "UPDATE user_games SET finished_at = ?, duration = ?, correct_guesses = ?, "
                        + "incorrect_guesses = ? WHERE game_id = ? AND finished_at IS NULL",
                now, durationSeconds, correct, incorrect, gameId);
        if (updated == 0) {
            throw new IllegalStateException("game already ended: " + gameId);
        }
        return new GameEndResult(correct, incorrect, durationSeconds, accuracyPercent);
    }
}