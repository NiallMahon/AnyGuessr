package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryQuizSessionRepositoryTest {

    @Test
    void savedQuizSessionCanBeFoundById() {
        QuizSessionId sessionId =
                new QuizSessionId("quiz-1");

        QuizSession session = new QuizSession(
                sessionId,
                new MapId("world-map"),
                List.of(
                        new RegionId("FR"),
                        new RegionId("DE")
                )
        );

        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        repository.save(session);

        QuizSession found =
                repository.findById(sessionId);

        assertSame(session, found);
    }

    @Test
    void unknownQuizSessionReturnsNull() {
        InMemoryQuizSessionRepository repository =
                new InMemoryQuizSessionRepository();

        QuizSession found =
                repository.findById(
                        new QuizSessionId("missing")
                );

        assertNull(found);
    }
}