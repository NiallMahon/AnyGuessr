package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryQuizSessionRepository
        implements QuizSessionRepository {

    private final Map<QuizSessionId, QuizSession> sessions =
            new HashMap<>();

    @Override
    public QuizSession findById(QuizSessionId id) {
        return sessions.get(id);
    }

    @Override
    public void save(QuizSession quizSession) {
        sessions.put(
                quizSession.getSessionId(),
                quizSession
        );
    }
}