package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.List;
import java.util.UUID;

/** Handles the creation of new quiz sessions. */
public class StartQuizUseCase {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizMapRepository quizMapRepository;
    private final QuizEventListener quizEventListener;

    /** Creates the use case with its required collaborators. */
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

    /** Starts a new quiz for the specified map. */
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
                session.getCurrentTarget(),
                session.snapshot().targetRegionIds()
        );
    }
}