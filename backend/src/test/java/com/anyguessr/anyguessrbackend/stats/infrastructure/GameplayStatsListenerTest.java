// Integration test from gameplay's quiz events, through the database, to the stats API, including ranking and deleting.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.WeakRegion;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.GameResultResponse;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.MapComparisonResponse;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.MapDetailResponse;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.UserStatsResponse;

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
    void playerQuizAndGuessesAreRecordedAndGuestQuizIsSkipped() throws Exception {
        listener.quizStarted(new QuizSessionId("player-quiz"), 1L, new MapId("1"));
        listener.guessMade(new QuizSessionId("player-quiz"), new RegionId("1"), new RegionId("2"), false, 53.3, -6.2);
        listener.guessMade(new QuizSessionId("player-quiz"), new RegionId("1"), new RegionId("1"), true, null, null);
        listener.quizFinished(new QuizSessionId("player-quiz"), 1, 2);
        listener.quizStarted(new QuizSessionId("guest-quiz"), null, new MapId("1"));
        listener.guessMade(new QuizSessionId("guest-quiz"), new RegionId("1"), new RegionId("1"), true, null, null);
        listener.quizFinished(new QuizSessionId("guest-quiz"), 2, 2);
        listener.awaitQueue();

        UserStatsResponse stats = statsController.getUserStats(1);

        assertEquals(1, stats.gamesPlayed());
        assertEquals(50.0, stats.accuracyPercent());
        assertEquals("Europe", stats.recentGames().get(0).mapName());
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM guesses", Integer.class));
        assertEquals(53.3, jdbc.queryForObject("SELECT clicked_lat FROM guesses WHERE attempt_number = 1", Double.class));
    }

    @Test
    void bestGameIsRankedEarnsTrophiesAndCanBeDeleted() throws Exception {
        listener.quizStarted(new QuizSessionId("first-quiz"), 1L, new MapId("1"));
        listener.guessMade(new QuizSessionId("first-quiz"), new RegionId("1"), new RegionId("1"), true, null, null);
        listener.quizFinished(new QuizSessionId("first-quiz"), 1, 2);
        listener.quizStarted(new QuizSessionId("second-quiz"), 1L, new MapId("1"));
        listener.quizFinished(new QuizSessionId("second-quiz"), 3, 3);
        listener.awaitQueue();

        UserStatsResponse stats = statsController.getUserStats(1);

        assertEquals(100.0, stats.bestPerMap().get(0).accuracyPercent());
        assertEquals(1, stats.bestPerMap().get(0).place());
        assertEquals("FLAWLESS", stats.trophies().get(1).type());
        assertNotNull(stats.trophies().get(1).awardedAt());
        assertEquals("player-one", statsController.getLeaderboard(1).get(0).username());

        statsController.deleteUserStats(1);

        assertEquals(0, statsController.getUserStats(1).gamesPlayed());
        assertNull(statsController.getUserStats(1).trophies().get(1).awardedAt());
        assertTrue(statsController.getLeaderboard(1).isEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM guesses", Integer.class));
    }

    @Test
    void gameResultMapDetailAndComparisonComeFromRecordedGames() throws Exception {
        play("slip-one", 1, false);
        play("slip-two", 1, false);
        play("perfect", 1, true);
        play("rival", 2, false);
        listener.awaitQueue();

        GameResultResponse slip = statsController.getGameResult("slip-one").getBody();
        GameResultResponse perfect = statsController.getGameResult("perfect").getBody();
        MapDetailResponse detail = statsController.getMapDetail(1, 1);
        MapComparisonResponse comparison = statsController.compare(1, 2).get(0);

        assertEquals(List.of("France"), slip.missedRegions());
        assertTrue(slip.newTrophies().contains("First steps"));
        assertTrue(perfect.newBest());
        assertEquals(66.67, perfect.previousBestPercent(), 0.01);
        assertEquals(1, perfect.place());
        assertEquals(2, perfect.players());
        assertTrue(perfect.newTrophies().contains("Flawless"));
        assertEquals(3, detail.gamesPlayed());
        assertEquals(List.of(new WeakRegion("France", 3, 1, "Germany")), detail.weakRegions());
        assertEquals(1, comparison.yours().place());
        assertEquals(2, comparison.theirs().place());
        assertEquals(404, statsController.getGameResult("unknown").getStatusCode().value());
    }

    private void play(String quiz, long userId, boolean firstGuessRight) {
        QuizSessionId session = new QuizSessionId(quiz);
        RegionId france = new RegionId("1");

        listener.quizStarted(session, userId, new MapId("1"));
        listener.guessMade(session, france, firstGuessRight ? france : new RegionId("2"), firstGuessRight, null, null);
        if (!firstGuessRight) {
            listener.guessMade(session, france, france, true, null, null);
        }
        listener.guessMade(session, new RegionId("2"), new RegionId("2"), true, null, null);
        listener.guessMade(session, new RegionId("3"), new RegionId("3"), true, null, null);
        listener.quizFinished(session, firstGuessRight ? 3 : 2, 3);
    }
}
