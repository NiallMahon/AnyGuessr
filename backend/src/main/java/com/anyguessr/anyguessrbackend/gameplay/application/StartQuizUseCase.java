package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.List;
import java.util.UUID;

public class StartQuizUseCase {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizMapRepository quizMapRepository;

    public StartQuizUseCase(
            QuizSessionRepository quizSessionRepository,
            QuizMapRepository quizMapRepository
    ) {
        this.quizSessionRepository = quizSessionRepository;
        this.quizMapRepository = quizMapRepository;
    }

    public StartQuizResult execute(MapId mapId) {

        List<RegionId> regionIds =
                quizMapRepository.findRegionIdsByMapId(mapId);

        QuizSessionId sessionId =
                new QuizSessionId(
                        UUID.randomUUID().toString()
                );

        QuizSession session =
                new QuizSession(
                        sessionId,
                        mapId,
                        regionIds
                );

        quizSessionRepository.save(session);

        return new StartQuizResult(
                sessionId,
                mapId,
                session.getCurrentTarget()
        );
    }
}