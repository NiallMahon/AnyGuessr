// Group-level test: a whole game through StatsService and the database, then on the profile.
package com.anyguessr.anyguessrbackend.stats.application;

import com.anyguessr.anyguessrbackend.stats.application.StatsService.GuessDetails;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.UserStats;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.infrastructure.JdbcGameRecordRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:stats-test.properties")
@Sql("classpath:stats-test-data.sql")
class StatsServiceTest {

    private static final GameSessionId SESSION = new GameSessionId("quiz-1");
    private static final UserId PLAYER = new UserId(1);
    private static final RegionId FRANCE = new RegionId(1);
    private static final RegionId GERMANY = new RegionId(2);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void finishedGameShowsOnTheProfile() {
        StatsService statsService = new StatsService(
                new JdbcGameRecordRepository(jdbcTemplate),
                Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneOffset.UTC));

        statsService.startGame(SESSION, PLAYER, new MapId(1));
        statsService.recordGuess(SESSION, new GuessDetails(FRANCE, GERMANY, false, null, null));
        statsService.recordGuess(SESSION, new GuessDetails(FRANCE, FRANCE, true, null, null));
        statsService.recordGuess(SESSION, new GuessDetails(GERMANY, GERMANY, true, null, null));
        statsService.finishGame(SESSION);

        UserStats stats = statsService.getUserStats(PLAYER);

        assertEquals(1, stats.totals().gamesPlayed());
        assertEquals(50.0, stats.totals().accuracyPercent());
        assertEquals(SESSION, stats.recentGames().get(0).sessionId());
    }
}
