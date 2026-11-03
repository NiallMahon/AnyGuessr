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
    private final QuizEventListener quizEventListener;

    public StartQuizUseCase(
            QuizSessionRepository quizSessionRepository,
            QuizMapRepository quizMapRepository,
            QuizEventListener quizEventListener
    ) {
        if (quizEventListener == null) {
            throw new IllegalArgumentException(
                    "Quiz event listener must not be null"
            );
        }

        this.quizSessionRepository = quizSessionRepository;
        this.quizMapRepository = quizMapRepository;
        this.quizEventListener = quizEventListener;
    }

    public StartQuizResult execute(
            MapId mapId,
            Long userId
    ) {

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

        quizEventListener.quizStarted(
                sessionId,
                userId,
                mapId
        );

        return new StartQuizResult(
                sessionId,
                mapId,
                session.getCurrentTarget()
        );
    }
}