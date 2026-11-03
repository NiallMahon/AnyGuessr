package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StartQuizUseCaseTest {

    @Test
    void startQuizCreatesAndSavesSessionForMapRegions() {

        MapId mapId = new MapId("1");

        List<RegionId> regions = List.of(
                new RegionId("1"),
                new RegionId("2"),
                new RegionId("3")
        );

        QuizMapRepository mapRepository =
                requestedMapId -> regions;

        InMemoryQuizSessionRepository sessionRepository =
                new InMemoryQuizSessionRepository();

        StartQuizUseCase useCase =
                new StartQuizUseCase(
                        sessionRepository,
                        mapRepository,
                        QuizEventListener.NONE
                );

        StartQuizResult result =
                useCase.execute(mapId, 7L);

        assertNotNull(result.sessionId());
        assertEquals(mapId, result.mapId());
        assertTrue(
                regions.contains(result.currentTarget())
        );

        QuizSession savedSession =
                sessionRepository.findById(
                        result.sessionId()
                );

        assertNotNull(savedSession);
        assertEquals(
                mapId,
                savedSession.getMapId()
        );
        assertEquals(
                result.currentTarget(),
                savedSession.getCurrentTarget()
        );
    }

    @Test
    void startQuizRejectsMapWithNoRegions() {

        MapId mapId = new MapId("1");

        QuizMapRepository mapRepository =
                requestedMapId -> List.of();

        InMemoryQuizSessionRepository sessionRepository =
                new InMemoryQuizSessionRepository();

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        StartQuizUseCase useCase =
                new StartQuizUseCase(
                        sessionRepository,
                        mapRepository,
                        listener
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(mapId, 7L)
        );

        assertTrue(listener.events().isEmpty());
    }

    @Test
    void startQuizAnnouncesQuizStartedWithPlayerAndMap() {

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        StartQuizUseCase useCase =
                new StartQuizUseCase(
                        new InMemoryQuizSessionRepository(),
                        requestedMapId -> List.of(
                                new RegionId("1")
                        ),
                        listener
                );

        StartQuizResult result =
                useCase.execute(
                        new MapId("1"),
                        7L
                );

        assertEquals(
                List.of(
                        "started " + result.sessionId().value()
                                + " user=7 map=1"
                ),
                listener.events()
        );
    }

    @Test
    void startQuizForPlayerWhoIsNotLoggedInAnnouncesNoUser() {

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        StartQuizUseCase useCase =
                new StartQuizUseCase(
                        new InMemoryQuizSessionRepository(),
                        requestedMapId -> List.of(
                                new RegionId("1")
                        ),
                        listener
                );

        StartQuizResult result =
                useCase.execute(
                        new MapId("1"),
                        null
                );

        assertEquals(
                List.of(
                        "started " + result.sessionId().value()
                                + " user=null map=1"
                ),
                listener.events()
        );
    }

    @Test
    void quizStartedIsAnnouncedOnlyAfterSessionIsSaved() {

        InMemoryQuizSessionRepository sessionRepository =
                new InMemoryQuizSessionRepository();

        List<Boolean> savedWhenAnnounced =
                new ArrayList<>();

        QuizEventListener listener =
                new QuizEventListener() {
                    @Override
                    public void quizStarted(
                            QuizSessionId sessionId,
                            Long userId,
                            MapId mapId
                    ) {
                        savedWhenAnnounced.add(
                                sessionRepository.findById(sessionId)
                                        != null
                        );
                    }
                };

        StartQuizUseCase useCase =
                new StartQuizUseCase(
                        sessionRepository,
                        requestedMapId -> List.of(
                                new RegionId("1")
                        ),
                        listener
                );

        useCase.execute(
                new MapId("1"),
                7L
        );

        assertEquals(
                List.of(true),
                savedWhenAnnounced
        );
    }
}
