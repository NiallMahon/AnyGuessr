package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

/**
 * Port that lets other features hear when a quiz starts, gets a guess and
 * finishes, without gameplay depending on them. Every method does nothing by
 * default such that you only need to implement the events you need.
 */
public interface QuizEventListener {

    /**
     * A listener that ignores every event.
     */
    QuizEventListener NONE =
            new QuizEventListener() {
            };

    /**
     * Called when a quiz starts.
     *
     * @param sessionId the quiz session
     * @param userId    the player, or {@code null} for a guest
     * @param mapId     the map being played
     */
    default void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            MapId mapId
    ) {
    }

    /**
     * Called when a guess is made.
     *
     * @param sessionId  the quiz session
     * @param target     the region the player was asked to find
     * @param selected   the region the player clicked
     * @param correct    whether the guess was right
     * @param clickedLat latitude of the click, or {@code null}
     * @param clickedLng longitude of the click, or {@code null}
     */
    default void guessMade(
            QuizSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
    }

    /**
     * Called when a quiz finishes.
     *
     * @param sessionId       the quiz session
     * @param correctAnswers  targets right on the first try
     * @param answeredTargets targets answered in total
     */
    default void quizFinished(
            QuizSessionId sessionId,
            int correctAnswers,
            int answeredTargets
    ) {
    }
}
