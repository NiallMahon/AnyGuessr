// Function- and file-level tests for GameRecord: the scoring, then a whole game.
package com.anyguessr.anyguessrbackend.stats.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameRecordTest {

    private static final Instant START = Instant.parse("2026-11-02T10:00:00Z");
    private static final RegionId FRANCE = new RegionId(1);
    private static final RegionId GERMANY = new RegionId(2);
    private static final RegionId SPAIN = new RegionId(3);

    private static GameRecord newGame() {
        return GameRecord.start(new GameSessionId("quiz-1"), new UserId(7), new MapId(3), START);
    }

    @Test
    void finishWorksOutCorrectIncorrectDurationAndAccuracy() {
        GameRecord game = newGame();
        game.recordGuess(FRANCE, FRANCE, 0, 0, START.plusSeconds(10));
        game.recordGuess(GERMANY, null, 0, 0, START.plusSeconds(15));
        game.recordGuess(GERMANY, FRANCE, 0, 0, START.plusSeconds(20));
        game.recordGuess(GERMANY, GERMANY, 0, 0, START.plusSeconds(25));
        game.recordGuess(SPAIN, SPAIN, 0, 0, START.plusSeconds(30));

        GameRecord.Summary summary = game.finish(START.plusSeconds(95));

        assertEquals(2, summary.correctGuesses());
        assertEquals(1, summary.incorrectGuesses());
        assertEquals(95, summary.durationSeconds());
        assertEquals(66.67, summary.accuracyPercent(), 0.01);
    }

    @Test
    void gameRecordsNumberedGuessesThenFinishes() {
        GameRecord game = newGame();

        GameRecord.Guess first = game.recordGuess(FRANCE, GERMANY, 0, 0, START.plusSeconds(5));
        GameRecord.Guess second = game.recordGuess(FRANCE, FRANCE, 0, 0, START.plusSeconds(9));
        game.finish(START.plusSeconds(60));

        assertEquals(2, second.attemptNumber());
        assertEquals(List.of(first, second), game.getGuesses());
        assertTrue(game.isFinished());
    }
}
