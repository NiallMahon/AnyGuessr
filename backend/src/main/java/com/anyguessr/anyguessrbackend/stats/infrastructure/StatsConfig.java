// Creates the stats classes for Spring.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;

@Configuration
public class StatsConfig {

    @Bean
    public Clock statsClock() {
        return Clock.systemUTC();
    }

    @Bean
    public GameRecordRepository gameRecordRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcGameRecordRepository(jdbcTemplate);
    }

    @Bean
    public StatsService statsService(
            GameRecordRepository gameRecordRepository,
            Clock statsClock
    ) {
        return new StatsService(gameRecordRepository, statsClock);
    }
}
