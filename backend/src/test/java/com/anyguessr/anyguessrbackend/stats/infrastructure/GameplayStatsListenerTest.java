// End-to-end backend test: gameplay's announcements for a quiz, through the database, out of the stats API.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController;
import com.anyguessr.anyguessrbackend.stats.ui.StatsController.UserStatsResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:stats-test.properties")
@Sql("classpath:stats-test-data.sql")
class GameplayStatsListenerTest {

    private static final QuizSessionId QUIZ = new QuizSessionId("quiz-1");
    private static final MapId EUROPE = new MapId("1");

    @Autowired
    private QuizEventListener listener;

    @Autowired
    private StatsController statsController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void wholeQuizShowsOnTheProfile() {
        listener.quizStarted(QUIZ, 1L, EUROPE);
        listener.quizFinished(QUIZ, 1, 2);

        UserStatsResponse stats = statsController.getUserStats(1);

        assertEquals(1, stats.gamesPlayed());
        assertEquals(50.0, stats.accuracyPercent());
        assertEquals("Europe", stats.recentGames().get(0).mapName());
    }

    @Test
    void guestQuizIsNotRecorded() {
        listener.quizStarted(QUIZ, null, EUROPE);
        listener.quizFinished(QUIZ, 1, 1);

        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_games", Integer.class));
    }
}
