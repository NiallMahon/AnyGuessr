// Records gameplay's quiz start, guesses and final score as stats games.
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

/**
 * Adapter that listens to gameplay's quiz events and records them as stats;
 * translates gameplay's IDs into stats' own IDs.
 */
public class GameplayStatsListener implements QuizEventListener {

    private final StatsService statsService;

    /**
     * Creates the listener.
     *
     * @param statsService the stats service to record games with
     * @throws IllegalArgumentException if the service is {@code null}
     */
    public GameplayStatsListener(StatsService statsService) {
        if (statsService == null) {
            throw new IllegalArgumentException("Stats service must not be null");
        }

        this.statsService = statsService;
    }

    /**
     * Starts a stats game when a signed-in player starts a quiz.
     *
     * @param sessionId the quiz session
     * @param userId    the player, or {@code null} for a guest
     * @param mapId     the map being played
     */
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

    /**
     * Records a guess made in a quiz.
     *
     * @param sessionId  the quiz session
     * @param target     the region the player was asked to find
     * @param selected   the region the player clicked
     * @param correct    whether the guess was right
     * @param clickedLat latitude of the click, or {@code null}
     * @param clickedLng longitude of the click, or {@code null}
     */
    @Override
    public void guessMade(
            QuizSessionId sessionId,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId target,
            com.anyguessr.anyguessrbackend.gameplay.domain.RegionId selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng
    ) {
        statsService.recordGuess(
                new GameSessionId(sessionId.value()),
                new GameRecord.Guess(
                        new RegionId(Long.parseLong(target.value())),
                        new RegionId(Long.parseLong(selected.value())),
                        correct,
                        clickedLat,
                        clickedLng
                )
        );
    }

    /**
     * Finishes the stats game with the quiz's final score.
     *
     * @param sessionId       the quiz session
     * @param correctAnswers  targets right on the first try
     * @param answeredTargets targets answered in total
     */
    @Override
    @Transactional
    public void quizFinished(QuizSessionId sessionId, int correctAnswers, int answeredTargets) {
        statsService.finishGame(
                new GameSessionId(sessionId.value()),
                new Score(correctAnswers, answeredTargets - correctAnswers)
        );
    }
}
