// Records gameplay's quiz start, guesses and final score as stats games, in order on one background thread so gameplay never waits.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameplayStatsListener implements QuizEventListener, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(GameplayStatsListener.class);

    private final StatsService statsService;
    private final ExecutorService queue = Executors.newSingleThreadExecutor();

    public GameplayStatsListener(StatsService statsService) {
        if (statsService == null) {
            throw new IllegalArgumentException("Stats service must not be null");
        }

        this.statsService = statsService;
    }

    @Override
    public void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            com.anyguessr.anyguessrbackend.gameplay.domain.MapId mapId
    ) {
        if (userId == null) {
            return;
        }

        enqueue("start of quiz " + sessionId.value(), () -> statsService.startGame(
                new GameSessionId(sessionId.value()),
                new UserId(userId),
                new MapId(Long.parseLong(mapId.value()))
        ));
    }

    @Override
    public void guessMade(
            QuizSessionId sessionId,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId target,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
        enqueue("guess in quiz " + sessionId.value(), () -> statsService.recordGuess(
                new GameSessionId(sessionId.value()),
                new RegionId(Long.parseLong(target.value())),
                new RegionId(Long.parseLong(selected.value())),
                correct,
                clickedLat,
                clickedLng
        ));
    }

    @Override
    public void quizFinished(QuizSessionId sessionId, int correctAnswers, int answeredTargets) {
        enqueue("finish of quiz " + sessionId.value(), () -> statsService.finishGame(
                new GameSessionId(sessionId.value()),
                new Score(correctAnswers, answeredTargets - correctAnswers)
        ));
    }

    @Override
    public void close() {
        queue.close();
    }

    void awaitQueue() throws InterruptedException, ExecutionException {
        queue.submit(() -> { }).get();
    }

    private void enqueue(String event, Runnable work) {
        queue.execute(() -> {
            try {
                work.run();
            } catch (RuntimeException e) {
                log.error("Stats could not record the {}", event, e);
            }
        });
    }
}
