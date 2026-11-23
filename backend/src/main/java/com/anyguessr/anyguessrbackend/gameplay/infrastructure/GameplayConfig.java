package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;

import org.springframework.beans.factory.ObjectProvider;
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
            QuizMapRepository quizMapRepository,
            ObjectProvider<QuizEventListener> quizEventListener
    ) {
        return new StartQuizUseCase(
                quizSessionRepository,
                quizMapRepository,
                safe(quizEventListener)
        );
    }

    @Bean
    public SubmitGuessUseCase submitGuessUseCase(
            QuizSessionRepository quizSessionRepository,
            ObjectProvider<QuizEventListener> quizEventListener
    ) {
        return new SubmitGuessUseCase(
                quizSessionRepository,
                safe(quizEventListener)
        );
    }

    private QuizEventListener safe(
            ObjectProvider<QuizEventListener> quizEventListener
    ) {
        return new SafeQuizEventListener(
                quizEventListener.getIfAvailable(
                        () -> QuizEventListener.NONE
                )
        );
    }
}