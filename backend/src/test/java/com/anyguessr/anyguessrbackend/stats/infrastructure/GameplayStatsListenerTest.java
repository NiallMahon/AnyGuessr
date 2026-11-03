// Group-level test: gameplay's announcements for a quiz, recorded through StatsService and the database.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.UserStats;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.junit.jupiter.api.BeforeEach;
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
class GameplayStatsListenerTest {

    private static final QuizSessionId QUIZ = new QuizSessionId("quiz-1");
    private static final MapId EUROPE = new MapId("1");
    private static final RegionId FRANCE = new RegionId("1");
    private static final RegionId GERMANY = new RegionId("2");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SpringDataUserGameRepository userGameRepository;

    @Autowired
    private SpringDataGuessRepository guessRepository;

    private StatsService statsService;
    private GameplayStatsListener listener;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(
                new JpaGameRecordRepository(userGameRepository, guessRepository),
                Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneOffset.UTC));
        listener = new GameplayStatsListener(statsService);
    }

    @Test
    void wholeQuizShowsOnTheProfile() {
        listener.quizStarted(QUIZ, 1L, EUROPE);
        listener.guessMade(QUIZ, FRANCE, GERMANY, false);
        listener.guessMade(QUIZ, GERMANY, GERMANY, true);
        listener.quizFinished(QUIZ);

        UserStats stats = statsService.getUserStats(new UserId(1));

        assertEquals(1, stats.totals().gamesPlayed());
        assertEquals(50.0, stats.totals().accuracyPercent());
    }

    @Test
    void guestQuizIsNotRecorded() {
        listener.quizStarted(QUIZ, null, EUROPE);
        listener.guessMade(QUIZ, FRANCE, FRANCE, true);
        listener.quizFinished(QUIZ);

        assertEquals(0, countGames());
    }

    @Test
    void statsProblemDoesNotStopTheQuiz() {
        long unknownUser = 99;

        assertDoesNotThrow(() -> {
            listener.quizStarted(QUIZ, unknownUser, EUROPE);
            listener.guessMade(QUIZ, FRANCE, FRANCE, true);
            listener.quizFinished(QUIZ);
        });

        assertEquals(0, countGames());
    }

    private int countGames() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_games", Integer.class);
    }
}
