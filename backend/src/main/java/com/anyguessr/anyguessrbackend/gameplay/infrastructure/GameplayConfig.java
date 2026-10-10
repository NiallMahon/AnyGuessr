package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameplayConfig {

    @Bean
    public QuizSessionRepository quizSessionRepository() {
        return new InMemoryQuizSessionRepository();
    }

    @Bean
    public SubmitGuessUseCase submitGuessUseCase(
            QuizSessionRepository quizSessionRepository
    ) {
        return new SubmitGuessUseCase(
                quizSessionRepository
        );
    }
}