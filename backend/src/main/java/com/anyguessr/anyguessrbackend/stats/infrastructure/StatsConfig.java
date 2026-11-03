// Creates the stats classes for Spring.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class StatsConfig {

    @Bean
    public Clock statsClock() {
        return Clock.systemUTC();
    }

    @Bean
    public GameRecordRepository gameRecordRepository(
            SpringDataUserGameRepository userGameRepository,
            SpringDataGuessRepository guessRepository
    ) {
        return new JpaGameRecordRepository(userGameRepository, guessRepository);
    }

    @Bean
    public StatsService statsService(
            GameRecordRepository gameRecordRepository,
            Clock statsClock
    ) {
        return new StatsService(gameRecordRepository, statsClock);
    }

    // Gameplay picks this up and announces every quiz to it.
    @Bean
    public QuizEventListener gameplayStatsListener(StatsService statsService) {
        return new GameplayStatsListener(statsService);
    }
}
