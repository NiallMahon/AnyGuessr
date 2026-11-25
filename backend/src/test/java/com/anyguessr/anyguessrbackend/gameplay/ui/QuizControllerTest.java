package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionSnapshot;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizStatus;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the quiz REST endpoints. */
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
        assertEquals(
                response.currentTarget(),
                response.targets().get(0)
        );
    }

    @Test
    void submitGuessIsQueuedThenScored() {

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
                new SubmitGuessRequest("FR", null, null);

        controller.submitGuess(
                "quiz-1",
                request
        );
        controller.close();

        QuizSessionSnapshot snapshot =
                repository.findById(sessionId).snapshot();

        assertEquals(
                1,
                snapshot.correctAnswers()
        );
        assertEquals(
                1,
                snapshot.answeredTargets()
        );
        assertEquals(
                QuizStatus.COMPLETE,
                snapshot.status()
        );
    }
}