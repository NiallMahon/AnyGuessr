// Scores a guess on a saved quiz and announces the guess and any final score.
package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

public class SubmitGuessUseCase {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizEventListener quizEventListener;

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