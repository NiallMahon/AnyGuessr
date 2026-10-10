package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubmitGuessUseCaseTest {

    @Test
    void submitGuessLoadsSessionAndReturnsDomainResult() {
        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        RegionId france =
                new RegionId("FR");

        QuizSession session = new QuizSession(
                sessionId,
                new MapId("world-map"),
                List.of(france)
        );

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        repository.save(session);

        SubmitGuessUseCase useCase =
                new SubmitGuessUseCase(repository);

        GuessResult result =
                useCase.execute(
                        sessionId,
                        france
                );

        assertTrue(result.correct());
        assertEquals(1, result.correctAnswers());
        assertEquals(1, result.answeredTargets());
        assertEquals(100.0, result.accuracy());
        assertTrue(result.quizComplete());
    }

    @Test
    void missingQuizSessionIsRejected() {
        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        SubmitGuessUseCase useCase =
                new SubmitGuessUseCase(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        new QuizSessionId("missing"),
                        new RegionId("FR")
                )
        );
    }
}