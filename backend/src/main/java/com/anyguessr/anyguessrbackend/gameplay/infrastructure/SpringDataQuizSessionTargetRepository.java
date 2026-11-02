package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataQuizSessionTargetRepository
        extends JpaRepository<
                QuizSessionTargetEntity,
                QuizSessionTargetId
        > {

    List<QuizSessionTargetEntity>
    findByIdSessionIdOrderByIdTargetPositionAsc(
            String sessionId
    );
}