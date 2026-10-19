package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JpaQuizSessionRepositoryIntegrationTest {

    @Autowired
    private SpringDataQuizSessionRepository sessionRepository;

    @Autowired
    private SpringDataQuizSessionTargetRepository targetRepository;

    @Test
    void savesAndRestoresQuizSessionState() {

        JpaQuizSessionRepository repository =
                new JpaQuizSessionRepository(
                        sessionRepository,
                        targetRepository
                );

        QuizSessionId sessionId =
                new QuizSessionId(
                        "11111111-1111-1111-1111-111111111111"
                );

        QuizSession original =
                new QuizSession(
                        sessionId,
                        new MapId("1"),
                        List.of(
                                new RegionId("10"),
                                new RegionId("20"),
                                new RegionId("30")
                        )
                );

        RegionId firstTarget =
                original.getCurrentTarget();

        original.submitGuess(firstTarget);

        RegionId secondTarget =
                original.getCurrentTarget();

        RegionId wrongRegion =
                List.of(
                        new RegionId("10"),
                        new RegionId("20"),
                        new RegionId("30")
                )
                .stream()
                .filter(region ->
                        !region.equals(secondTarget))
                .findFirst()
                .orElseThrow();

        original.submitGuess(wrongRegion);

        repository.save(original);

        QuizSession restored =
                repository.findById(sessionId);

        assertNotNull(restored);

        assertEquals(
                original.getSessionId(),
                restored.getSessionId()
        );

        assertEquals(
                original.getMapId(),
                restored.getMapId()
        );

        assertEquals(
                original.getCurrentTarget(),
                restored.getCurrentTarget()
        );

        /*
         * If correction state survived persistence,
         * correcting the target must not increase
         * answeredTargets again.
         */
        GuessResult correction =
                restored.submitGuess(secondTarget);

        assertTrue(correction.correct());
        assertFalse(correction.correctionRequired());
        assertEquals(1, correction.correctAnswers());
        assertEquals(2, correction.answeredTargets());
        assertEquals(50.0, correction.accuracy());
    }
}