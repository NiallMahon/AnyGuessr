package com.anyguessr.anyguessrbackend.gameplay.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizSessionIdTest {

    @Test
    void validQuizSessionIdStoresValue() {
        QuizSessionId sessionId = new QuizSessionId("quiz-1");

        assertEquals("quiz-1", sessionId.value());
    }

    @Test
    void blankQuizSessionIdIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuizSessionId(" ")
        );
    }

    @Test
    void quizSessionIdsWithSameValueAreEqual() {
        QuizSessionId first = new QuizSessionId("quiz-1");
        QuizSessionId second = new QuizSessionId("quiz-1");

        assertEquals(first, second);
    }
}