// Scores a guess on a saved quiz and announces the guess and any final score.
package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

/**
 * Handles the submission and processing of quiz guesses.
 *
 * Retrieves the current quiz session, evaluates the selected
 * region, saves the updated session and publishes gameplay events.
 */

public class SubmitGuessUseCase {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizEventListener quizEventListener;

    /**
     * Creates the use case with its required collaborators.
     *
     * @param quizSessionRepository retrieves and saves quiz sessions
     * @param quizEventListener receives gameplay events
     * @throws IllegalArgumentException if either dependency is null
    */

    public SubmitGuessUseCase(
            QuizSessionRepository quizSessionRepository,
            QuizEventListener quizEventListener
    ) {
        if (quizSessionRepository == null) {
            throw new IllegalArgumentException(
                    "Quiz session repository must not be null"
            );
        }

        if (quizEventListener == null) {
            throw new IllegalArgumentException(
                    "Quiz event listener must not be null"
            );
        }

        this.quizSessionRepository =
                quizSessionRepository;

        this.quizEventListener =
                quizEventListener;
    }

    /**
     * Processes a player's guess for an existing quiz session.
     *
     * @param sessionId identifies the quiz session
     * @param selectedRegionId identifies the region selected by the player
     * @param clickedLat optional latitude of the player's click
     * @param clickedLng optional longitude of the player's click
     * @return the guess result and updated quiz progress
     * @throws IllegalArgumentException if the quiz session is not found
    */

    public GuessResult execute(
            QuizSessionId sessionId,
            RegionId selectedRegionId,
            Double clickedLat,
            Double clickedLng
    ) {
        QuizSession session =
                quizSessionRepository.findById(sessionId);

        if (session == null) {
            throw new IllegalArgumentException(
                    "Quiz session not found"
            );
        }

        GuessResult result =
                session.submitGuess(selectedRegionId);

        quizSessionRepository.save(session);

        quizEventListener.guessMade(
                sessionId,
                result.targetRegionId(),
                result.selectedRegionId(),
                result.correct(),
                clickedLat,
                clickedLng
        );

        if (result.quizComplete()) {
            quizEventListener.quizFinished(
                    sessionId,
                    result.correctAnswers(),
                    result.answeredTargets()
            );
        }

        return result;
    }
}