package com.anyguessr.anyguessrbackend.gameplay.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class QuizSessionTest {

    @Test
    void quizSessionBelongsToSelectedMap() {
        RegionId france = new RegionId("FR");
        MapId worldMap = new MapId("world-map");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                worldMap,
                List.of(france)
        );

        assertEquals(worldMap, session.getMapId());
    }

    @Test
    void correctFirstGuessIsRecordedAsCorrect() {
        RegionId france = new RegionId("FR");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                List.of(france)
        );

        GuessResult result = session.submitGuess(france);

        assertTrue(result.correct());
        assertFalse(result.correctionRequired());
        assertEquals(1, result.correctAnswers());
        assertEquals(1, result.answeredTargets());
        assertEquals(100.0, result.accuracy());
        assertTrue(result.quizComplete());
    }

    @Test
    void incorrectFirstGuessRequiresCorrectionAndReducesAccuracy() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                List.of(france, germany),
                new Random(123)
        );

        GuessResult result = session.submitGuess(germany);

        assertFalse(result.correct());
        assertTrue(result.correctionRequired());
        assertEquals(france, result.targetRegionId());
        assertEquals(0, result.correctAnswers());
        assertEquals(1, result.answeredTargets());
        assertEquals(0.0, result.accuracy());
        assertFalse(result.quizComplete());
    }

    @Test
    void correctiveClickDoesNotChangeAccuracy() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                List.of(france, germany),
                new Random(123)
        );

        session.submitGuess(germany);

        GuessResult correction = session.submitGuess(france);

        assertTrue(correction.correct());
        assertFalse(correction.correctionRequired());
        assertEquals(0, correction.correctAnswers());
        assertEquals(1, correction.answeredTargets());
        assertEquals(0.0, correction.accuracy());
        assertFalse(correction.quizComplete());
    }

    @Test
    void wrongClickDuringCorrectionDoesNotChangeAccuracyOrProgress() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");
        RegionId spain = new RegionId("ES");

        List<RegionId> regions = List.of(
                france,
                germany,
                spain
        );

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                regions,
                new Random(123)
        );

        RegionId target = session.getCurrentTarget();

        List<RegionId> wrongRegions = regions.stream()
                .filter(region -> !region.equals(target))
                .toList();

        // First wrong click is the one scored attempt.
        session.submitGuess(wrongRegions.get(0));

        // A second wrong click during correction must not affect accuracy.
        GuessResult secondWrongClick =
                session.submitGuess(wrongRegions.get(1));

        assertFalse(secondWrongClick.correct());
        assertTrue(secondWrongClick.correctionRequired());

        assertEquals(
                target,
                secondWrongClick.targetRegionId()
        );

        assertEquals(
                0,
                secondWrongClick.correctAnswers()
        );

        assertEquals(
                1,
                secondWrongClick.answeredTargets()
        );

        assertEquals(
                0.0,
                secondWrongClick.accuracy()
        );

        assertFalse(secondWrongClick.quizComplete());
    }

    @Test
    void targetOrderIsRandomisedButContainsOnlyQuizRegions() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");
        RegionId spain = new RegionId("ES");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-random"),
                new MapId("world-map"),
                List.of(france, germany, spain),
                new Random(123)
        );

        RegionId currentTarget =
                session.getCurrentTarget();

        assertTrue(
                List.of(france, germany, spain)
                        .contains(currentTarget)
        );
    }

    @Test
    void completedTargetIsNotSelectedAgain() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");
        RegionId spain = new RegionId("ES");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                List.of(france, germany, spain),
                new Random(123)
        );

        RegionId firstTarget =
                session.getCurrentTarget();

        session.submitGuess(firstTarget);

        RegionId secondTarget =
                session.getCurrentTarget();

        assertNotEquals(
                firstTarget,
                secondTarget
        );
    }

    @Test
    void regionOutsideQuizIsRejected() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");
        RegionId brazil = new RegionId("BR");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                List.of(france, germany),
                new Random(123)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session.submitGuess(brazil)
        );
    }

    @Test
    void guessAfterQuizCompletionIsRejected() {
        RegionId france = new RegionId("FR");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
                new MapId("world-map"),
                List.of(france)
        );

        RegionId target =
                session.getCurrentTarget();

        session.submitGuess(target);

        assertThrows(
                IllegalStateException.class,
                () -> session.submitGuess(target)
        );
    }
@Test
void restoredSessionPreservesProgressAndCorrectionState() {

    RegionId france = new RegionId("FR");
    RegionId germany = new RegionId("DE");
    RegionId spain = new RegionId("ES");

    List<RegionId> regions = List.of(
            france,
            germany,
            spain
    );

    QuizSession original = new QuizSession(
            new QuizSessionId("quiz-restore"),
            new MapId("world-map"),
            regions,
            new Random(123)
    );

    /*
     * Complete the first target correctly.
     */
    RegionId firstTarget =
            original.getCurrentTarget();

    original.submitGuess(firstTarget);

    /*
     * Make a wrong first attempt on the second target.
     * This puts the session into correction mode.
     */
    RegionId secondTarget =
            original.getCurrentTarget();

    RegionId wrongRegion = regions.stream()
            .filter(region ->
                    !region.equals(secondTarget))
            .findFirst()
            .orElseThrow();

    original.submitGuess(wrongRegion);

    /*
     * Simulate saving and reloading the domain state.
     */
    QuizSessionSnapshot snapshot =
            original.snapshot();

    QuizSession restored =
            QuizSession.restore(snapshot);

    assertEquals(
            original.getSessionId(),
            restored.getSessionId()
    );

    assertEquals(
            original.getMapId(),
            restored.getMapId()
    );

    assertEquals(
            secondTarget,
            restored.getCurrentTarget()
    );

    /*
     * Because correctionRequired must survive restoration,
     * clicking the correct region must NOT count as another
     * scored attempt.
     */
    GuessResult correction =
            restored.submitGuess(secondTarget);

    assertTrue(correction.correct());
    assertFalse(correction.correctionRequired());

    assertEquals(
            1,
            correction.correctAnswers()
    );

    assertEquals(
            2,
            correction.answeredTargets()
    );

    assertEquals(
            50.0,
            correction.accuracy()
    );

    assertFalse(
            correction.quizComplete()
    );

    /*
     * Both completed targets must remain completed,
     * so the restored session must now move to the
     * remaining third target.
     */
    assertNotEquals(
            firstTarget,
            restored.getCurrentTarget()
    );

    assertNotEquals(
            secondTarget,
            restored.getCurrentTarget()
    );
}
}