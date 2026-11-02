package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameplayConfig {

    @Bean
    public QuizSessionRepository quizSessionRepository(
            SpringDataQuizSessionRepository sessionRepository,
            SpringDataQuizSessionTargetRepository targetRepository
    ) {
        return new JpaQuizSessionRepository(
                sessionRepository,
                targetRepository
        );
    }

    @Bean
    public QuizMapRepository quizMapRepository(
            SpringDataRegionRepository regionRepository
    ) {
        return new JpaQuizMapRepository(
                regionRepository
        );
    }

    @Bean
    public StartQuizUseCase startQuizUseCase(
            QuizSessionRepository quizSessionRepository,
            QuizMapRepository quizMapRepository
    ) {
        return new StartQuizUseCase(
                quizSessionRepository,
                quizMapRepository
        );
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