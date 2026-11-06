package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataQuizSessionRepository
        extends JpaRepository<QuizSessionEntity, String> {
}