// Hears gameplay's quiz announcements and records them as stats games.
// The only stats class that knows about gameplay: it turns gameplay's IDs into stats' own.
// Gameplay logs any failure here, so a stats problem never stops a quiz.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

public class GameplayStatsListener implements QuizEventListener {

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
        if (userId == null) {
            return;
        }

        statsService.startGame(
                new GameSessionId(sessionId.value()),
                new UserId(userId),
                new MapId(Long.parseLong(mapId.value()))
        );
    }

    @Override
    public void quizFinished(QuizSessionId sessionId, int correctAnswers, int answeredTargets) {
        statsService.finishGame(
                new GameSessionId(sessionId.value()),
                new Score(correctAnswers, answeredTargets - correctAnswers)
        );
    }
}
