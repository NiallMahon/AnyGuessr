// End-to-end test from gameplay's quiz events, through the database, to the stats API.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.UserStatsResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:stats-test.properties")
@Sql("classpath:stats-test-data.sql")
class GameplayStatsListenerTest {

    @Autowired
    private QuizEventListener listener;

    @Autowired
    private StatsController statsController;

    @Test
    void playerQuizShowsOnTheProfileAndGuestQuizIsSkipped() {
        listener.quizStarted(new QuizSessionId("player-quiz"), 1L, new MapId("1"));
        listener.quizFinished(new QuizSessionId("player-quiz"), 1, 2);
        listener.quizStarted(new QuizSessionId("guest-quiz"), null, new MapId("1"));
        listener.quizFinished(new QuizSessionId("guest-quiz"), 2, 2);

        UserStatsResponse stats = statsController.getUserStats(1);

        assertEquals(1, stats.gamesPlayed());
        assertEquals(50.0, stats.accuracyPercent());
        assertEquals("Europe", stats.recentGames().get(0).mapName());
    }
}
