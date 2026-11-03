// Unit test for finishing a GameRecord with its score.
package com.anyguessr.anyguessrbackend.stats.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

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
}
