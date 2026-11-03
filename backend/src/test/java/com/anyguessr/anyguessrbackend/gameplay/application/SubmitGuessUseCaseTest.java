package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
                new SubmitGuessUseCase(
                        repository,
                        QuizEventListener.NONE
                );

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
                new SubmitGuessUseCase(
                        repository,
                        QuizEventListener.NONE
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        new QuizSessionId("missing"),
                        new RegionId("FR")
                )
        );
    }

    @Test
    void finishedQuizAnnouncesTheServersScore() {
        RegionId france = new RegionId("FR");
        RegionId germany = new RegionId("DE");
        QuizSession session = new QuizSession(new QuizSessionId("quiz-1"), new MapId("world-map"), List.of(france, germany));
        RegionId first = session.getCurrentTarget();
        RegionId second = first.equals(france) ? germany : france;

        InMemoryQuizSessionRepository repository = new InMemoryQuizSessionRepository();
        repository.save(session);

        List<String> finished = new ArrayList<>();
        SubmitGuessUseCase useCase = new SubmitGuessUseCase(repository, new QuizEventListener() {
            @Override
            public void quizFinished(QuizSessionId id, int correctAnswers, int answeredTargets) {
                finished.add(correctAnswers + "/" + answeredTargets);
            }
        });

        useCase.execute(session.getSessionId(), second);
        useCase.execute(session.getSessionId(), first);
        useCase.execute(session.getSessionId(), second);

        assertEquals(List.of("1/2"), finished);
    }
}
