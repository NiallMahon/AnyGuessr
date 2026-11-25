package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Records gameplay's quiz events as stats, translating gameplay IDs into stats IDs. */
public class GameplayStatsListener implements QuizEventListener {

    private final StatsService statsService;

    /** Creates the listener. */
    public GameplayStatsListener(StatsService statsService) {
        if (statsService == null) {
            throw new IllegalArgumentException("Stats service must not be null");
        }

        this.statsService = statsService;
    }

    /** Starts a stats game when a signed-in player starts a quiz. */
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

    /** Records a guess made in a quiz. */
    @Override
    public void guessMade(
            QuizSessionId sessionId,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId target,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng,
            Instant guessedAt
    ) {
        statsService.recordGuess(
                new GameSessionId(sessionId.value()),
                new GameRecord.Guess(
                        new RegionId(Long.parseLong(target.value())),
                        new RegionId(Long.parseLong(selected.value())),
                        correct,
                        clickedLat,
                        clickedLng
                ),
                guessedAt
        );
    }

    /** Finishes the stats game with the quiz's final score. */
    @Override
    @Transactional
    public void quizFinished(QuizSessionId sessionId, int correctAnswers, int answeredTargets, Instant finishedAt) {
        statsService.finishGame(
                new GameSessionId(sessionId.value()),
                new Score(correctAnswers, answeredTargets - correctAnswers),
                finishedAt
        );
    }
}
