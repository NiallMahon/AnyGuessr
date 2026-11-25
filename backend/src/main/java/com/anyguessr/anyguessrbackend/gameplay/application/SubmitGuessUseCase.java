package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.time.Instant;

/** Handles the submission and processing of quiz guesses. */
public class SubmitGuessUseCase {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizEventListener quizEventListener;

    /** Creates the use case with its required collaborators. */
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

    /** Processes a player's guess for an existing quiz session. */
    public GuessResult execute(
            QuizSessionId sessionId,
            RegionId selectedRegionId,
            Double clickedLat,
            Double clickedLng,
            Instant guessedAt
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
                clickedLng,
                guessedAt
        );

        if (result.quizComplete()) {
            quizEventListener.quizFinished(
                    sessionId,
                    result.correctAnswers(),
                    result.answeredTargets(),
                    guessedAt
            );
        }

        return result;
    }
}