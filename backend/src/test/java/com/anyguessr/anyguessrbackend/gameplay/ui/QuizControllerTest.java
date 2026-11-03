package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.RecordingQuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuizControllerTest {

    @Test
    void startQuizReturnsSessionAndFirstTarget() {

        RegionId france =
                new RegionId("FR");

        RegionId germany =
                new RegionId("DE");

        QuizMapRepository mapRepository =
                requestedMapId -> List.of(
                        france,
                        germany
                );

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        StartQuizUseCase startQuizUseCase =
                new StartQuizUseCase(
                        repository,
                        mapRepository,
                        QuizEventListener.NONE
                );

        SubmitGuessUseCase submitGuessUseCase =
                new SubmitGuessUseCase(
                        repository,
                        QuizEventListener.NONE
                );

        QuizController controller =
                new QuizController(
                        startQuizUseCase,
                        submitGuessUseCase
                );

        StartQuizRequest request =
                new StartQuizRequest("world-map", null);

        StartQuizResponse response =
                controller.startQuiz(request);

        assertNotNull(response.sessionId());
        assertEquals(
                "world-map",
                response.mapId()
        );
        assertTrue(
                List.of("FR", "DE")
                        .contains(response.currentTarget())
        );
    }

    @Test
    void submitGuessReturnsQuizResultResponse() {

        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        RegionId france =
                new RegionId("FR");

        QuizSession session =
                new QuizSession(
                        sessionId,
                        new MapId("world-map"),
                        List.of(france)
                );

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        repository.save(session);

        QuizMapRepository mapRepository =
                requestedMapId -> List.of(france);

        StartQuizUseCase startQuizUseCase =
                new StartQuizUseCase(
                        repository,
                        mapRepository,
                        QuizEventListener.NONE
                );

        SubmitGuessUseCase submitGuessUseCase =
                new SubmitGuessUseCase(
                        repository,
                        QuizEventListener.NONE
                );

        QuizController controller =
                new QuizController(
                        startQuizUseCase,
                        submitGuessUseCase
                );

        SubmitGuessRequest request =
                new SubmitGuessRequest("FR");

        GuessResponse response =
                controller.submitGuess(
                        "quiz-1",
                        request
                );

        assertTrue(response.correct());
        assertFalse(response.correctionRequired());
        assertEquals(
                "FR",
                response.selectedRegionId()
        );
        assertEquals(
                "FR",
                response.targetRegionId()
        );
        assertEquals(
                1,
                response.correctAnswers()
        );
        assertEquals(
                1,
                response.answeredTargets()
        );
        assertEquals(
                100.0,
                response.accuracy()
        );
        assertTrue(response.quizComplete());
    }

    @Test
    void startQuizPassesLoggedInPlayerToListener() {

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        StartQuizUseCase startQuizUseCase =
                new StartQuizUseCase(
                        repository,
                        requestedMapId -> List.of(
                                new RegionId("FR")
                        ),
                        listener
                );

        SubmitGuessUseCase submitGuessUseCase =
                new SubmitGuessUseCase(
                        repository,
                        listener
                );

        QuizController controller =
                new QuizController(
                        startQuizUseCase,
                        submitGuessUseCase
                );

        StartQuizResponse response =
                controller.startQuiz(
                        new StartQuizRequest("1", 7L)
                );

        assertEquals(
                List.of(
                        "started " + response.sessionId()
                                + " user=7 map=1"
                ),
                listener.events()
        );
    }
}