// Passes quiz events on and logs any listener failure, so another feature can't break gameplay.
package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeQuizEventListener implements QuizEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(SafeQuizEventListener.class);

    private final QuizEventListener listener;

    public SafeQuizEventListener(
            QuizEventListener listener
    ) {
        if (listener == null) {
            throw new IllegalArgumentException(
                    "Quiz event listener must not be null"
            );
        }

        this.listener = listener;
    }

    @Override
    public void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            MapId mapId
    ) {
        announce(
                "quiz started",
                sessionId,
                () -> listener.quizStarted(
                        sessionId,
                        userId,
                        mapId
                )
        );
    }

    @Override
    public void guessMade(
            QuizSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
        announce(
                "guess made",
                sessionId,
                () -> listener.guessMade(
                        sessionId,
                        target,
                        selected,
                        correct,
                        clickedLat,
                        clickedLng
                )
        );
    }

    @Override
    public void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets
    ) {
        announce(
                "quiz finished",
                sessionId,
                () -> listener.quizFinished(
                        sessionId,
                        correctAnswers,
                        answeredTargets
                )
        );
    }

    private void announce(
            String event,
            QuizSessionId sessionId,
            Runnable announcement
    ) {
        try {
            announcement.run();
        } catch (RuntimeException e) {
            log.warn(
                    "Quiz event listener failed on {} for session {}",
                    event,
                    sessionId.value(),
                    e
            );
        }
    }
}
