package com.anyguessr.anyguessrbackend.gameplay.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;


class QuizSessionTest {

    @Test
    void correctFirstGuessIsRecordedAsCorrect() {
        RegionId france = new RegionId("FR");

        QuizSession session = new QuizSession(
                new QuizSessionId("quiz-1"),
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
        List.of(france, germany),
        new Random(123)
);

    session.submitGuess(germany);   // wrong first attempt

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

    List<RegionId> regions = List.of(france, germany, spain);

    QuizSession session = new QuizSession(
            new QuizSessionId("quiz-1"),
            regions,
            new Random(123)
    );

    RegionId target = session.getCurrentTarget();

    List<RegionId> wrongRegions = regions.stream()
            .filter(region -> !region.equals(target))
            .toList();

    // First wrong click: this is the one scored attempt.
    session.submitGuess(wrongRegions.get(0));

    // Another valid map region is clicked while correction is required.
    GuessResult secondWrongClick =
            session.submitGuess(wrongRegions.get(1));

    assertFalse(secondWrongClick.correct());
    assertTrue(secondWrongClick.correctionRequired());

    assertEquals(target, secondWrongClick.targetRegionId());

    assertEquals(0, secondWrongClick.correctAnswers());
    assertEquals(1, secondWrongClick.answeredTargets());
    assertEquals(0.0, secondWrongClick.accuracy());

    assertFalse(secondWrongClick.quizComplete());
}
	@Test
void targetOrderIsRandomisedButContainsOnlyQuizRegions() {
    RegionId france = new RegionId("FR");
    RegionId germany = new RegionId("DE");
    RegionId spain = new RegionId("ES");

    QuizSession session = new QuizSession(
            new QuizSessionId("quiz-random"),
            List.of(france, germany, spain),
            new Random(123)
    );

    RegionId currentTarget = session.getCurrentTarget();

    assertTrue(
            List.of(france, germany, spain).contains(currentTarget)
    );
}

	@Test
void completedTargetIsNotSelectedAgain() {
    RegionId france = new RegionId("FR");
    RegionId germany = new RegionId("DE");
    RegionId spain = new RegionId("ES");

    QuizSession session = new QuizSession(
            new QuizSessionId("quiz-1"),
            List.of(france, germany, spain),
            new Random(123)
    );

    RegionId firstTarget = session.getCurrentTarget();

    session.submitGuess(firstTarget);

    RegionId secondTarget = session.getCurrentTarget();

    assertNotEquals(firstTarget, secondTarget);
}
	@Test
void regionOutsideQuizIsRejected() {
    RegionId france = new RegionId("FR");
    RegionId germany = new RegionId("DE");
    RegionId brazil = new RegionId("BR");

    QuizSession session = new QuizSession(
            new QuizSessionId("quiz-1"),
            List.of(france, germany),
            new Random(123)
    );

    assertThrows(
            IllegalArgumentException.class,
            () -> session.submitGuess(brazil)
    );
}
}