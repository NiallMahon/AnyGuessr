package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Checks a failing quiz event listener can't stop a quiz. */
class SafeQuizEventListenerTest {

    @Test
    void failingListenerDoesNotStopTheQuiz() {
        SafeQuizEventListener safeListener = new SafeQuizEventListener(new QuizEventListener() {
            @Override
            public void quizFinished(QuizSessionId sessionId, int correctAnswers, int answeredTargets, Instant finishedAt) {
                throw new IllegalStateException("Database is down");
            }
        });

        assertDoesNotThrow(() -> safeListener.quizFinished(new QuizSessionId("quiz-1"), 1, 2, Instant.now()));
    }
}
