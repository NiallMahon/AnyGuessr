package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Passes quiz events on and logs listener failures, so stats can't break gameplay. */
public class SafeQuizEventListener implements QuizEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(SafeQuizEventListener.class);

    private final QuizEventListener listener;

    /** Wraps a listener. */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void guessMade(
            QuizSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng,
            Instant guessedAt
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
                        clickedLng,
                        guessedAt
                )
        );
    }

    /** {@inheritDoc} */
    @Override
    public void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets,
            Instant finishedAt
    ) {
        announce(
                "quiz finished",
                sessionId,
                () -> listener.quizFinished(
                        sessionId,
                        correctAnswers,
                        answeredTargets,
                        finishedAt
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
