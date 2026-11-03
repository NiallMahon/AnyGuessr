package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SafeQuizEventListenerTest {

    @Test
    void failingListenerDoesNotStopTheQuiz() {

        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        SafeQuizEventListener safeListener =
                new SafeQuizEventListener(
                        new QuizEventListener() {
                            @Override
                            public void quizStarted(
                                    QuizSessionId sessionId,
                                    Long userId,
                                    MapId mapId
                            ) {
                                throw new IllegalArgumentException("Unknown user");
                            }

                            @Override
                            public void quizFinished(
                                    QuizSessionId sessionId,
                                    int correctAnswers,
                                    int answeredTargets
                            ) {
                                throw new IllegalStateException("Database is down");
                            }
                        }
                );

        assertDoesNotThrow(() -> {
            safeListener.quizStarted(sessionId, 7L, new MapId("1"));
            safeListener.quizFinished(sessionId, 1, 2);
        });
    }
}
