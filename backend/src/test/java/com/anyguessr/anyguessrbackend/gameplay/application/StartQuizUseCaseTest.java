package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;
import com.anyguessr.anyguessrbackend.gameplay.infrastructure.InMemoryQuizSessionRepository;

import org.junit.jupiter.api.Test;

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
                        mapRepository
                );

        StartQuizResult result =
                useCase.execute(mapId);

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

        StartQuizUseCase useCase =
                new StartQuizUseCase(
                        sessionRepository,
                        mapRepository
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(mapId)
        );
    }
}