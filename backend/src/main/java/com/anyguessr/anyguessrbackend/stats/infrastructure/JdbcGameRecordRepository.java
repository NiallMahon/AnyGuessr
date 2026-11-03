// Saves games and works out profile stats with SQL.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.GameAlreadyFinishedException;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Guess;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Summary;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcGameRecordRepository implements GameRecordRepository {

    private record GameRow(long userId, long mapId, Instant startedAt, Instant finishedAt) {}

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insertGame;

    public JdbcGameRecordRepository(JdbcTemplate jdbc) {
        if (jdbc == null) {
            throw new IllegalArgumentException("JdbcTemplate must not be null");
        }

        this.jdbc = jdbc;
        this.insertGame = new SimpleJdbcInsert(jdbc)
                .withTableName("user_games")
                .usingColumns("session_id", "user_id", "map_id", "started_at",
                        "correct_guesses", "incorrect_guesses");
    }

    @Override
    public Optional<GameRecord> findBySessionId(GameSessionId sessionId) {
        List<GameRow> games = jdbc.query(
                "SELECT user_id, map_id, started_at, finished_at FROM user_games WHERE session_id = ?",
                (rs, rowNum) -> new GameRow(
                        rs.getLong("user_id"),
                        rs.getLong("map_id"),
                        toInstant(rs.getTimestamp("started_at")),
                        toInstant(rs.getTimestamp("finished_at"))),
                sessionId.value());

        if (games.isEmpty()) {
            return Optional.empty();
        }

        GameRow game = games.get(0);

        List<Guess> guesses = jdbc.query(
                "SELECT g.attempt_number, g.target_region_id, g.selected_region_id, g.correct, "
                        + "g.clicked_lat, g.clicked_lng, g.guessed_at "
                        + "FROM guesses g JOIN user_games ug ON ug.game_id = g.game_id "
                        + "WHERE ug.session_id = ? ORDER BY g.attempt_number",
                JdbcGameRecordRepository::toGuess,
                sessionId.value());

        return Optional.of(GameRecord.restore(
                sessionId,
                new UserId(game.userId()),
                new MapId(game.mapId()),
                game.startedAt(),
                game.finishedAt(),
                guesses));
    }

    @Override
    public boolean userExists(UserId userId) {
        return exists("SELECT COUNT(*) FROM users WHERE user_id = ?", userId.value());
    }

    @Override
    public boolean mapExists(MapId mapId) {
        return exists("SELECT COUNT(*) FROM map_data WHERE map_id = ?", mapId.value());
    }

    @Override
    public boolean regionIsOnMap(RegionId regionId, MapId mapId) {
        return exists("SELECT COUNT(*) FROM regions WHERE region_id = ? AND map_id = ?",
                regionId.value(), mapId.value());
    }

    @Override
    public void add(GameRecord gameRecord) {
        insertGame.execute(Map.of(
                "session_id", gameRecord.getSessionId().value(),
                "user_id", gameRecord.getUserId().value(),
                "map_id", gameRecord.getMapId().value(),
                "started_at", Timestamp.from(gameRecord.getStartedAt()),
                "correct_guesses", 0,
                "incorrect_guesses", 0));
    }

    @Override
    public void addGuess(GameRecord gameRecord, Guess guess) {
        jdbc.update(
                "INSERT INTO guesses (game_id, attempt_number, map_id, target_region_id, "
                        + "selected_region_id, correct, clicked_lat, clicked_lng, guessed_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                findGameId(gameRecord.getSessionId()),
                guess.attemptNumber(),
                gameRecord.getMapId().value(),
                guess.targetRegionId().value(),
                guess.selectedRegionId() == null ? null : guess.selectedRegionId().value(),
                guess.correct(),
                guess.lat(),
                guess.lng(),
                Timestamp.from(guess.guessedAt()));
    }

    @Override
    public void markFinished(GameRecord gameRecord) {
        Summary summary = gameRecord.summary();

        int updated = jdbc.update(
                "UPDATE user_games SET finished_at = ?, duration = ?, correct_guesses = ?, "
                        + "incorrect_guesses = ? WHERE session_id = ? AND finished_at IS NULL",
                Timestamp.from(gameRecord.getFinishedAt().orElseThrow()),
                summary.durationSeconds(),
                summary.correctGuesses(),
                summary.incorrectGuesses(),
                gameRecord.getSessionId().value());

        if (updated == 0) {
            throw new GameAlreadyFinishedException(gameRecord.getSessionId());
        }
    }

    @Override
    public PlayerTotals findTotalsForUser(UserId userId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) AS games_played, "
                        + "COALESCE(SUM(correct_guesses), 0) AS correct, "
                        + "COALESCE(SUM(incorrect_guesses), 0) AS incorrect, "
                        + "MIN(duration) AS best_time, AVG(duration) AS average_time "
                        + "FROM user_games WHERE user_id = ? AND finished_at IS NOT NULL",
                (rs, rowNum) -> new PlayerTotals(
                        rs.getInt("games_played"),
                        rs.getInt("correct"),
                        rs.getInt("incorrect"),
                        rs.getObject("best_time", Integer.class),
                        toWholeSeconds(rs.getBigDecimal("average_time"))),
                userId.value());
    }

    @Override
    public List<FinishedGame> findRecentFinishedGames(UserId userId, int limit) {
        return jdbc.query(
                "SELECT ug.session_id, m.map_name, ug.finished_at, ug.correct_guesses, "
                        + "ug.incorrect_guesses, ug.duration "
                        + "FROM user_games ug JOIN map_data m ON m.map_id = ug.map_id "
                        + "WHERE ug.user_id = ? AND ug.finished_at IS NOT NULL "
                        + "ORDER BY ug.finished_at DESC, ug.game_id DESC LIMIT ?",
                (rs, rowNum) -> new FinishedGame(
                        new GameSessionId(rs.getString("session_id")),
                        rs.getString("map_name"),
                        toInstant(rs.getTimestamp("finished_at")),
                        rs.getInt("correct_guesses"),
                        rs.getInt("incorrect_guesses"),
                        rs.getInt("duration")),
                userId.value(),
                limit);
    }

    private long findGameId(GameSessionId sessionId) {
        return jdbc.queryForObject(
                "SELECT game_id FROM user_games WHERE session_id = ?", Long.class, sessionId.value());
    }

    private boolean exists(String countQuery, Object... args) {
        Integer count = jdbc.queryForObject(countQuery, Integer.class, args);
        return count != null && count > 0;
    }

    private static Guess toGuess(ResultSet rs, int rowNum) throws SQLException {
        Long selectedRegionId = rs.getObject("selected_region_id", Long.class);

        return new Guess(
                rs.getInt("attempt_number"),
                new RegionId(rs.getLong("target_region_id")),
                selectedRegionId == null ? null : new RegionId(selectedRegionId),
                rs.getBoolean("correct"),
                rs.getObject("clicked_lat", Double.class),
                rs.getObject("clicked_lng", Double.class),
                toInstant(rs.getTimestamp("guessed_at")));
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Integer toWholeSeconds(BigDecimal seconds) {
        return seconds == null ? null : seconds.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
