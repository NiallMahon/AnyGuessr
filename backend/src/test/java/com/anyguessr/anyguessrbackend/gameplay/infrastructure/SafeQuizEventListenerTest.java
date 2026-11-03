package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.RecordingQuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafeQuizEventListenerTest {

    private final QuizSessionId sessionId =
            new QuizSessionId("quiz-1");

    private final RegionId france =
            new RegionId("FR");

    @Test
    void announcementsArePassedOnToTheListener() {

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        SafeQuizEventListener safeListener =
                new SafeQuizEventListener(listener);

        safeListener.quizStarted(sessionId, 7L, new MapId("1"));
        safeListener.guessMade(sessionId, france, france, true);
        safeListener.quizFinished(sessionId);

        assertEquals(
                List.of(
                        "started quiz-1 user=7 map=1",
                        "guess quiz-1 target=FR selected=FR correct=true",
                        "finished quiz-1"
                ),
                listener.events()
        );
    }

    @Test
    void failingListenerDoesNotStopTheQuiz() {

        QuizEventListener failingListener =
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
                    public void guessMade(
                            QuizSessionId sessionId,
                            RegionId target,
                            RegionId selected,
                            boolean correct
                    ) {
                        throw new IllegalStateException("No game found");
                    }

                    @Override
                    public void quizFinished(
                            QuizSessionId sessionId
                    ) {
                        throw new IllegalStateException("No game found");
                    }
                };

        SafeQuizEventListener safeListener =
                new SafeQuizEventListener(failingListener);

        assertDoesNotThrow(
                () -> safeListener.quizStarted(sessionId, null, new MapId("1"))
        );
        assertDoesNotThrow(
                () -> safeListener.guessMade(sessionId, france, france, true)
        );
        assertDoesNotThrow(
                () -> safeListener.quizFinished(sessionId)
        );
    }
}
