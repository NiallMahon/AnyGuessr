package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuizControllerTest {

    @Test
    void submitGuessReturnsQuizResultResponse() {
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

        QuizController controller =
                new QuizController(useCase);

        SubmitGuessRequest request =
                new SubmitGuessRequest("FR");

        GuessResponse response =
                controller.submitGuess(
                        "quiz-1",
                        request
                );

        assertTrue(response.correct());
        assertFalse(response.correctionRequired());
        assertEquals("FR", response.selectedRegionId());
        assertEquals("FR", response.targetRegionId());
        assertEquals(1, response.correctAnswers());
        assertEquals(1, response.answeredTargets());
        assertEquals(100.0, response.accuracy());
        assertTrue(response.quizComplete());
    }
}