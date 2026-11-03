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

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        SubmitGuessUseCase useCase =
                new SubmitGuessUseCase(
                        repository,
                        listener
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        new QuizSessionId("missing"),
                        new RegionId("FR")
                )
        );

        assertTrue(listener.events().isEmpty());
    }

    @Test
    void correctGuessOnLastTargetAnnouncesGuessThenFinish() {
        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        RegionId france =
                new RegionId("FR");

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        repository.save(
                new QuizSession(
                        sessionId,
                        new MapId("world-map"),
                        List.of(france)
                )
        );

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        SubmitGuessUseCase useCase =
                new SubmitGuessUseCase(
                        repository,
                        listener
                );

        useCase.execute(sessionId, france);

        assertEquals(
                List.of(
                        "guess quiz-1 target=FR selected=FR correct=true",
                        "finished quiz-1"
                ),
                listener.events()
        );
    }

    @Test
    void onlyTheScoredFirstAttemptAtEachTargetIsAnnounced() {
        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        RegionId france =
                new RegionId("FR");

        RegionId germany =
                new RegionId("DE");

        QuizSession session = new QuizSession(
                sessionId,
                new MapId("world-map"),
                List.of(france, germany)
        );

        /*
         * Targets are shuffled, so find out which one comes first.
         */
        RegionId first =
                session.getCurrentTarget();

        RegionId second =
                first.equals(france) ? germany : france;

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        repository.save(session);

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        SubmitGuessUseCase useCase =
                new SubmitGuessUseCase(
                        repository,
                        listener
                );

        /* Scored first attempt: wrong. */
        useCase.execute(sessionId, second);

        /* Correction clicks: wrong again, then right. */
        useCase.execute(sessionId, second);
        useCase.execute(sessionId, first);

        /* Scored first attempt on the last target: right. */
        useCase.execute(sessionId, second);

        assertEquals(
                List.of(
                        "guess quiz-1 target=" + first.value()
                                + " selected=" + second.value()
                                + " correct=false",
                        "guess quiz-1 target=" + second.value()
                                + " selected=" + second.value()
                                + " correct=true",
                        "finished quiz-1"
                ),
                listener.events()
        );
    }

    @Test
    void finishIsAnnouncedWhenCorrectionClickCompletesQuiz() {
        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        RegionId france =
                new RegionId("FR");

        RegionId germany =
                new RegionId("DE");

        QuizSession session = new QuizSession(
                sessionId,
                new MapId("world-map"),
                List.of(france, germany)
        );

        RegionId first =
                session.getCurrentTarget();

        RegionId second =
                first.equals(france) ? germany : france;

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        repository.save(session);

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        SubmitGuessUseCase useCase =
                new SubmitGuessUseCase(
                        repository,
                        listener
                );

        useCase.execute(sessionId, first);

        /* Scored first attempt on the last target: wrong. */
        useCase.execute(sessionId, first);

        /* Correction click that completes the quiz. */
        useCase.execute(sessionId, second);

        assertEquals(
                List.of(
                        "guess quiz-1 target=" + first.value()
                                + " selected=" + first.value()
                                + " correct=true",
                        "guess quiz-1 target=" + second.value()
                                + " selected=" + first.value()
                                + " correct=false",
                        "finished quiz-1"
                ),
                listener.events()
        );
    }
}
