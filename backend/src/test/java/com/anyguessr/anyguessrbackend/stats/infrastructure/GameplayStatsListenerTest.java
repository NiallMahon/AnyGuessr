// Integration test from gameplay's quiz events, through the database, to the stats API, including ranking and deleting.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.GameResultResponse;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.WeakRegionResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:stats-test.properties")
@Sql("classpath:stats-test-data.sql")
class GameplayStatsListenerTest {

    @Autowired
    private GameplayStatsListener listener;

    @Autowired
    private StatsController statsController;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void quizEventsReachEveryStatsEndpointAndCanBeDeleted() {
        play("slip", 1L, false);
        play("perfect", 1L, true);
        play("rival", 2L, false);
        play("guest", null, true);

        GameResultResponse slip = statsController.getGameResult("slip").getBody();
        GameResultResponse perfect = statsController.getGameResult("perfect").getBody();

        assertEquals(List.of("France"), slip.missedRegions());
        assertTrue(slip.newTrophies().contains("First steps"));
        assertTrue(perfect.newBest());
        assertEquals(66.67, perfect.previousBestPercent(), 0.01);
        assertEquals(1, perfect.place());
        assertEquals(2, perfect.players());
        assertTrue(perfect.newTrophies().contains("Flawless"));
        assertEquals(2, statsController.getUserStats(1).gamesPlayed());
        assertNotNull(statsController.getUserStats(1).trophies().get(1).awardedAt());
        assertEquals(List.of(new WeakRegionResponse("France", 2, 1, "Germany")), statsController.getMapDetail(1, 1).weakRegions());
        assertEquals(2, statsController.compare(1, 2).get(0).theirs().place());
        assertEquals("player-one", statsController.getLeaderboard(1).get(0).username());
        assertEquals(53.3, jdbc.queryForObject("SELECT MAX(clicked_lat) FROM guesses", Double.class));

        statsController.deleteUserStats(1);

        assertEquals(0, statsController.getUserStats(1).gamesPlayed());
        assertNull(statsController.getUserStats(1).trophies().get(1).awardedAt());
        assertEquals("player-two", statsController.getLeaderboard(1).get(0).username());
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM guesses", Integer.class));
    }

    private void play(String quiz, Long userId, boolean firstGuessRight) {
        QuizSessionId session = new QuizSessionId(quiz);
        RegionId france = new RegionId("1");

        listener.quizStarted(session, userId, new MapId("1"));
        listener.guessMade(session, france, firstGuessRight ? france : new RegionId("2"), firstGuessRight, 53.3, -6.2);
        if (!firstGuessRight) {
            listener.guessMade(session, france, france, true, null, null);
        }
        listener.guessMade(session, new RegionId("2"), new RegionId("2"), true, null, null);
        listener.guessMade(session, new RegionId("3"), new RegionId("3"), true, null, null);
        listener.quizFinished(session, firstGuessRight ? 3 : 2, 3);
    }
}
