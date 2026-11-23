package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.List;
import java.util.UUID;

/**
 * Handles the creation of new quiz sessions.
 *
 * Retrieves the regions associated with a map, creates and
 * persists a quiz session, and publishes a quiz-started event.
 */

public class StartQuizUseCase {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizMapRepository quizMapRepository;
    private final QuizEventListener quizEventListener;

    /**
     * Creates the use case with its required collaborators.
     *
     * @param quizSessionRepository stores quiz sessions
     * @param quizMapRepository retrieves regions for a selected map
     * @param quizEventListener receives quiz lifecycle events
     * @throws IllegalArgumentException if the event listener is null
    */

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

    /**
     * Starts a new quiz for the specified map.
     *
     * @param mapId identifies the selected map
     * @param userId identifies the player, or null for an anonymous session
     * @return the session ID, selected map ID and initial target region
    */

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