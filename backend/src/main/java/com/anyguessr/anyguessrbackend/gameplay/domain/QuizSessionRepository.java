package com.anyguessr.anyguessrbackend.gameplay.domain;

public interface QuizSessionRepository {

    QuizSession findById(QuizSessionId id);

    void save(QuizSession quizSession);
}