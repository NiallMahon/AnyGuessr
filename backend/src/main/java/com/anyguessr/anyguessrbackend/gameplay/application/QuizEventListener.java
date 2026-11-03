package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;

/*
 * Lets other features hear what happens in a quiz without
 * gameplay depending on them.
 *
 * Every method does nothing by default, so a listener only
 * needs to override the announcements it cares about.
 */
public interface QuizEventListener {

    /*
     * Used when nobody is listening.
     */
    QuizEventListener NONE =
            new QuizEventListener() {
            };

    /*
     * Announced after a new quiz session has been saved.
     * userId is null when the player is not logged in.
     */
    default void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            MapId mapId
    ) {
    }

    /*
     * Announced after the guess that completed the quiz
     * has been saved, with the quiz's final score.
     *
     * The score comes from the saved quiz session, not
     * from the player, so it cannot be edited.
     */
    default void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets
    ) {
    }
}
