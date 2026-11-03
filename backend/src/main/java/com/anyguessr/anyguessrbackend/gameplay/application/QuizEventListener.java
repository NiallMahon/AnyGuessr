// Lets other features hear when a quiz starts and finishes, without gameplay depending on them.
package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;

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

    default void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets
    ) {
    }
}
