// Creates the stats classes for Spring.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Spring configuration: wires stats classes together and plugs stats
 * into gameplay as a quiz event listener.
 */
@Configuration
public class StatsConfig {

    /**
     * Returns a UTC clock that ticks in whole seconds, used for game and trophy times.
     *
     * @return a UTC clock that ticks in whole seconds, used for game and trophy times
     */
    @Bean
    public Clock statsClock() {
        return Clock.tickSeconds(ZoneOffset.UTC);
    }

    /**
     * Returns the JPA-backed game record repository.
     *
     * @param userGameRepository the Spring Data repository for the stats tables
     * @return the JPA-backed game record repository
     */
    @Bean
    public GameRecordRepository gameRecordRepository(SpringDataUserGameRepository userGameRepository) {
        return new JpaGameRecordRepository(userGameRepository);
    }

    /**
     * Returns the stats service.
     *
     * @param gameRecordRepository where stats are stored
     * @param statsClock           the clock used for game and trophy times
     * @return the stats service
     */
    @Bean
    public StatsService statsService(
            GameRecordRepository gameRecordRepository,
            Clock statsClock
    ) {
        return new StatsService(gameRecordRepository, statsClock);
    }

    /**
     * Returns the listener that records gameplay's quiz events as stats.
     *
     * @param statsService the stats service
     * @return the listener that records gameplay's quiz events as stats
     */
    @Bean
    public QuizEventListener gameplayStatsListener(StatsService statsService) {
        return new GameplayStatsListener(statsService);
    }
}
