// Spring Data queries for the user_games table.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataUserGameRepository extends JpaRepository<UserGameEntity, Long> {

    interface TotalsRow {
        int getGamesPlayed();
        int getCorrect();
        int getIncorrect();
        Integer getBestTime();
        Double getAverageTime();
    }

    interface RecentGameRow {
        String getSessionId();
        String getMapName();
        LocalDateTime getFinishedAt();
        int getCorrectGuesses();
        int getIncorrectGuesses();
        int getDuration();
    }

    Optional<UserGameEntity> findBySessionId(String sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE UserGameEntity g SET g.finishedAt = :finishedAt, g.duration = :duration, "
            + "g.correctGuesses = :correct, g.incorrectGuesses = :incorrect "
            + "WHERE g.sessionId = :sessionId")
    void markFinished(String sessionId, Instant finishedAt, int duration, int correct, int incorrect);

    @Query("SELECT COUNT(g) AS gamesPlayed, "
            + "COALESCE(SUM(g.correctGuesses), 0) AS correct, "
            + "COALESCE(SUM(g.incorrectGuesses), 0) AS incorrect, "
            + "MIN(g.duration) AS bestTime, AVG(g.duration) AS averageTime "
            + "FROM UserGameEntity g WHERE g.userId = :userId AND g.finishedAt IS NOT NULL")
    TotalsRow findTotals(long userId);

    // Plain SQL: map names are in map_data, which stats has no entity for.
    @Query(value = "SELECT ug.session_id AS sessionId, m.map_name AS mapName, ug.finished_at AS finishedAt, "
            + "ug.correct_guesses AS correctGuesses, ug.incorrect_guesses AS incorrectGuesses, "
            + "ug.duration AS duration "
            + "FROM user_games ug JOIN map_data m ON m.map_id = ug.map_id "
            + "WHERE ug.user_id = :userId AND ug.finished_at IS NOT NULL "
            + "ORDER BY ug.finished_at DESC, ug.game_id DESC LIMIT :limit",
            nativeQuery = true)
    List<RecentGameRow> findRecentFinished(long userId, int limit);
}
