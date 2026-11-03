// Function- and file-level tests for GameRecord: finishing a game with its score.
package com.anyguessr.anyguessrbackend.stats.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class GameRecordTest {

    private static final Instant START = Instant.parse("2026-11-02T10:00:00Z");

    private final GameRecord game =
            GameRecord.start(new GameSessionId("quiz-1"), new UserId(7), new MapId(3), START);

    @Test
    void finishRecordsScoreAndDuration() {
        game.finish(START.plusSeconds(95), new Score(2, 1));

        assertEquals(95, game.durationSeconds());
        assertEquals(66.67, game.getScore().orElseThrow().accuracyPercent(), 0.01);
    }

    @Test
    void gameCannotFinishTwice() {
        game.finish(START.plusSeconds(60), new Score(1, 0));

        assertThrows(GameRecord.GameAlreadyFinishedException.class,
                () -> game.finish(START.plusSeconds(90), new Score(1, 0)));
    }
}
