// Lets other features hear when a quiz starts, gets a guess and finishes, without gameplay depending on them.
package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

public interface QuizEventListener {

    QuizEventListener NONE =
            new QuizEventListener() {
            };

    default void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            MapId mapId
    ) {
    }

    default void guessMade(
            QuizSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
    }

    default void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets
    ) {
    }
}
