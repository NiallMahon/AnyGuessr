// Unit tests for the stats rules: finishing a game, comparing games, trophies, streaks and weak regions.
package com.anyguessr.anyguessrbackend.stats.domain;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FirstGuess;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameRecordTest {

    @Test
    void finishRecordsScoreAndDuration() {
        Instant start = Instant.parse("2026-11-02T10:00:00Z");
        GameRecord game = GameRecord.start(new GameSessionId("quiz-1"), new UserId(7), new MapId(3), start);

        game.finish(start.plusSeconds(95), new Score(2, 1));

        assertEquals(95, game.durationSeconds());
        assertEquals(66.67, game.getScore().orElseThrow().accuracyPercent(), 0.01);
        assertThrows(GameRecord.GameAlreadyFinishedException.class,
                () -> game.finish(start.plusSeconds(99), new Score(2, 1)));
    }

    @Test
    void higherAccuracyWinsThenTheFasterGame() {
        GameRecord slowPerfect = finished("slow", 90, new Score(3, 0));
        GameRecord fastPerfect = finished("fast", 30, new Score(3, 0));
        GameRecord fastHalf = finished("half", 10, new Score(1, 1));

        assertTrue(fastPerfect.isBetterThan(slowPerfect));
        assertFalse(slowPerfect.isBetterThan(fastPerfect));
        assertTrue(slowPerfect.isBetterThan(fastHalf));
    }

    @Test
    void trophiesFollowTheirGoalsOnlyOnceAndIgnoreTinyGames() {
        Instant now = Instant.parse("2026-11-02T10:00:00Z");
        Trophy.Facts facts = Trophy.Facts.from(List.of(finished("perfect", 9, new Score(3, 0))), now);

        assertEquals(Set.of(Trophy.FIRST_GAME, Trophy.FLAWLESS, Trophy.SPEEDY), Trophy.newlyEarned(facts, Set.of()));
        assertEquals(Set.of(Trophy.SPEEDY), Trophy.newlyEarned(facts, Set.of(Trophy.FIRST_GAME, Trophy.FLAWLESS)));
        assertEquals(1, Trophy.REGULAR.progress(facts));
        assertEquals(10, Trophy.REGULAR.getGoal());
        assertEquals(Set.of(Trophy.FIRST_GAME),
                Trophy.newlyEarned(Trophy.Facts.from(List.of(finished("tiny", 1, new Score(1, 0))), now), Set.of()));
    }

    @Test
    void streakCountsWeeksInARowAndSurvivesUntilTheNextWeekEnds() {
        Instant monday = Instant.parse("2026-11-02T10:00:00Z");
        List<GameRecord> games = List.of(
                finishedOn(monday),
                finishedOn(monday.plusSeconds(WEEK)),
                finishedOn(monday.plusSeconds(3 * WEEK))
        );

        assertEquals(new PlayStreak(1, 2), PlayStreak.from(games, monday.plusSeconds(4 * WEEK)));
        assertEquals(new PlayStreak(0, 2), PlayStreak.from(games, monday.plusSeconds(5 * WEEK)));
    }

    @Test
    void weakRegionsKeepRepeatedMissesAndNameTheMixUp() {
        List<FirstGuess> guesses = List.of(
                new FirstGuess("France", "Germany", false),
                new FirstGuess("France", "Germany", false),
                new FirstGuess("France", "Spain", false),
                new FirstGuess("France", "France", true),
                new FirstGuess("Germany", "Germany", true),
                new FirstGuess("Germany", "Germany", true),
                new FirstGuess("Spain", "France", false)
        );

        assertEquals(List.of(new WeakRegion("France", 4, 1, "Germany")), WeakRegion.rank(guesses));
    }

    private static final int WEEK = 7 * 24 * 60 * 60;

    private static GameRecord finishedOn(Instant finishedAt) {
        GameRecord game = GameRecord.start(new GameSessionId("quiz"), new UserId(7), new MapId(3), finishedAt);
        game.finish(finishedAt, new Score(3, 0));
        return game;
    }

    private static GameRecord finished(String sessionId, int seconds, Score score) {
        Instant start = Instant.parse("2026-11-02T10:00:00Z");
        GameRecord game = GameRecord.start(new GameSessionId(sessionId), new UserId(7), new MapId(3), start);
        game.finish(start.plusSeconds(seconds), score);
        return game;
    }
}
