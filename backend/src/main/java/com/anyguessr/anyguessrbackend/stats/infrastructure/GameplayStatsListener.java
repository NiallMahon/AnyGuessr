// Hears gameplay's quiz announcements and records them as stats games.
// The only stats class that knows about gameplay.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.GameNotFoundException;
import com.anyguessr.anyguessrbackend.stats.application.StatsService.GuessDetails;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameplayStatsListener implements QuizEventListener {

    private static final Logger log = LoggerFactory.getLogger(GameplayStatsListener.class);

    private final StatsService statsService;

    public GameplayStatsListener(StatsService statsService) {
        if (statsService == null) {
            throw new IllegalArgumentException("Stats service must not be null");
        }

        this.statsService = statsService;
    }

    // Players who aren't logged in are not recorded.
    @Override
    public void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            com.anyguessr.anyguessrbackend.gameplay.domain.MapId mapId
    ) {
        if (userId == null || userId <= 0) {
            return;
        }

        try {
            statsService.startGame(
                    toGameSessionId(sessionId),
                    new UserId(userId),
                    new MapId(Long.parseLong(mapId.value()))
            );
        } catch (RuntimeException e) {
            log.warn("Stats could not record the start of quiz {}", sessionId.value(), e);
        }
    }

    @Override
    public void guessMade(
            QuizSessionId sessionId,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId target,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId selected,
            boolean correct
    ) {
        try {
            // Gameplay has no click coordinates, so they are left empty.
            GuessDetails details = new GuessDetails(
                    toRegionId(target),
                    toRegionId(selected),
                    correct,
                    null,
                    null
            );

            statsService.recordGuess(toGameSessionId(sessionId), details);
        } catch (GameNotFoundException e) {
            // Not recorded: a guest's quiz, or its start already failed and was logged.
        } catch (RuntimeException e) {
            log.warn("Stats could not record a guess in quiz {}", sessionId.value(), e);
        }
    }

    @Override
    public void quizFinished(QuizSessionId sessionId) {
        try {
            statsService.finishGame(toGameSessionId(sessionId));
        } catch (GameNotFoundException e) {
            // Not recorded: a guest's quiz, or its start already failed and was logged.
        } catch (RuntimeException e) {
            log.warn("Stats could not record the end of quiz {}", sessionId.value(), e);
        }
    }

    private static GameSessionId toGameSessionId(QuizSessionId sessionId) {
        return new GameSessionId(sessionId.value());
    }

    private static RegionId toRegionId(com.anyguessr.anyguessrbackend.gameplay.domain.RegionId regionId) {
        return new RegionId(Long.parseLong(regionId.value()));
    }
}
