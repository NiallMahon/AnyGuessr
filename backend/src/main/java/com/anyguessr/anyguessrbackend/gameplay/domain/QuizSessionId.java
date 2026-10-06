package com.anyguessr.anyguessrbackend.gameplay.domain;

public record QuizSessionId(String value) {

    public QuizSessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Quiz session ID must not be blank");
        }
    }
}