package com.anyguessr.anyguessrbackend.gameplay.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
            List.of(france, germany)
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
            List.of(france, germany)
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

    QuizSession session = new QuizSession(
            new QuizSessionId("quiz-1"),
            List.of(france, germany, spain)
    );

    session.submitGuess(germany); // first scored guess is wrong

    GuessResult secondWrongClick = session.submitGuess(spain);

    assertFalse(secondWrongClick.correct());
    assertTrue(secondWrongClick.correctionRequired());

    assertEquals(france, secondWrongClick.targetRegionId());

    assertEquals(0, secondWrongClick.correctAnswers());
    assertEquals(1, secondWrongClick.answeredTargets());
    assertEquals(0.0, secondWrongClick.accuracy());

    assertFalse(secondWrongClick.quizComplete());
}
}