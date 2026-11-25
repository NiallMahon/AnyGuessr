package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.time.Instant;

/** Port that lets other features hear quiz events without gameplay depending on them. */
public interface QuizEventListener {

    /** A listener that ignores every event. */
    QuizEventListener NONE =
            new QuizEventListener() {
            };

    /** Called when a quiz starts. */
    default void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            MapId mapId
    ) {
    }

    /** Called when a guess is made. */
    default void guessMade(
            QuizSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng,
            Instant guessedAt
    ) {
    }

    /** Called when a quiz finishes. */
    default void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets,
            Instant finishedAt
    ) {
    }
}
