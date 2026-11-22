// Spring Data queries for the user_games, guesses, leaderboard and user_trophies tables.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for the stats tables ({@code user_games}, {@code guesses},
 * {@code leaderboard} and {@code user_trophies}).
 */
public interface SpringDataUserGameRepository extends JpaRepository<UserGameEntity, Long> {

    /**
     * Query for a player's game count, right and wrong totals, best pace and average pace.
     */
    String TOTALS = "SELECT COUNT(g) AS gamesPlayed, "
            + "COALESCE(SUM(g.correctGuesses), 0) AS correct, "
            + "COALESCE(SUM(g.incorrectGuesses), 0) AS incorrect, "
            + "MIN(CASE WHEN g.correctGuesses * 100 >= :minPercent * (g.correctGuesses + g.incorrectGuesses) "
            + "THEN g.duration * 1.0 / NULLIF(g.correctGuesses + g.incorrectGuesses, 0) END) AS bestPace, "
            + "SUM(g.duration) * 1.0 / NULLIF(SUM(g.correctGuesses + g.incorrectGuesses), 0) AS averagePace "
            + "FROM UserGameEntity g WHERE g.userId = :userId AND g.finishedAt IS NOT NULL";

    /**
     * Start of the query for a player's finished games with the map name.
     */
    String RECENT = "SELECT ug.session_id AS sessionId, m.map_name AS mapName, ug.finished_at AS finishedAt, "
            + "ug.correct_guesses AS correctGuesses, ug.incorrect_guesses AS incorrectGuesses, "
            + "ug.duration AS duration "
            + "FROM user_games ug JOIN map_data m ON m.map_id = ug.map_id "
            + "WHERE ug.user_id = :userId AND ug.finished_at IS NOT NULL ";

    /**
     * Orders games newest first and limits how many are returned.
     */
    String NEWEST_FIRST = "ORDER BY ug.finished_at DESC, ug.game_id DESC LIMIT :limit";

    /**
     * Start of the query that numbers the guesses for each target so the first try can be picked out.
     */
    String FIRST_GUESSES = "SELECT t.name AS target, s.name AS selected, CASE WHEN g.correct THEN 1 ELSE 0 END AS correct "
            + "FROM (SELECT a.target_region_id, a.selected_region_id, a.correct, ROW_NUMBER() OVER "
            + "(PARTITION BY a.game_id, a.target_region_id ORDER BY a.attempt_number) AS try_number FROM guesses a ";

    /**
     * End of the first guesses query: keeps the first try and adds region names.
     */
    String FIRST_TRY_ONLY = ") g JOIN regions t ON t.region_id = g.target_region_id "
            + "JOIN regions s ON s.region_id = g.selected_region_id WHERE g.try_number = 1";

    /**
     * Query result for a player's totals on a map.
     */
    interface TotalsRow {
        int getGamesPlayed();
        int getCorrect();
        int getIncorrect();
        Double getBestPace();
        Double getAveragePace();
    }

    /**
     * Query result for the first guess at a target; {@code getCorrect()} is 1 or 0.
     */
    interface FirstGuessRow {
        String getTarget();
        String getSelected();
        int getCorrect();
    }

    /**
     * Query result for one finished game with its map name.
     */
    interface RecentGameRow {
        String getSessionId();
        String getMapName();
        LocalDateTime getFinishedAt();
        int getCorrectGuesses();
        int getIncorrectGuesses();
        int getDuration();
    }

    /**
     * Query result for a player's best game on a map with its leaderboard place.
     */
    interface MapBestRow {
        long getMapId();
        String getMapName();
        int getCorrectGuesses();
        int getIncorrectGuesses();
        int getDuration();
        int getPlace();
        int getPlayers();
    }

    /**
     * Query result for a player's overall place.
     */
    interface OverallRankRow {
        int getPlace();
        int getPlayers();
    }

    /**
     * Query result for one leaderboard row.
     */
    interface LeaderboardRow {
        int getPlace();
        long getUserId();
        String getUsername();
        double getAccuracy();
        int getDuration();
    }

    /**
     * Query result for one earned trophy.
     */
    interface TrophyRow {
        String getTrophyType();
        LocalDateTime getAwardedAt();
    }

    /**
     * Returns the game, or empty if none.
     *
     * @param sessionId the quiz session
     * @return the game, or empty if none
     */
    Optional<UserGameEntity> findBySessionId(String sessionId);

    /**
     * Saves a guess, numbering it after the game's earlier guesses.
     *
     * @param sessionId  the quiz session
     * @param target     the region asked for
     * @param selected   the region clicked
     * @param correct    whether the guess was right
     * @param clickedLat latitude of the click, or {@code null}
     * @param clickedLng longitude of the click, or {@code null}
     * @param guessedAt  when the guess was made
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO guesses (game_id, attempt_number, map_id, target_region_id, selected_region_id, "
            + "correct, clicked_lat, clicked_lng, guessed_at) "
            + "SELECT g.game_id, (SELECT COUNT(*) + 1 FROM guesses WHERE game_id = g.game_id), g.map_id, "
            + ":target, :selected, :correct, CAST(:clickedLat AS DOUBLE), CAST(:clickedLng AS DOUBLE), :guessedAt "
            + "FROM user_games g WHERE g.session_id = :sessionId",
            nativeQuery = true)
    void addGuess(
            String sessionId,
            long target,
            long selected,
            boolean correct,
            Double clickedLat,
            Double clickedLng,
            LocalDateTime guessedAt
    );

    /**
     * Saves the finish time, length and score of a game.
     *
     * @param sessionId  the quiz session
     * @param finishedAt when the game finished
     * @param duration   length in seconds
     * @param correct    targets right on the first try
     * @param incorrect  targets wrong on the first try
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserGameEntity g SET g.finishedAt = :finishedAt, g.duration = :duration, "
            + "g.correctGuesses = :correct, g.incorrectGuesses = :incorrect "
            + "WHERE g.sessionId = :sessionId")
    void markFinished(String sessionId, Instant finishedAt, int duration, int correct, int incorrect);

    /**
     * Returns the player's totals on the map.
     *
     * @param userId     the player
     * @param mapId      the map
     * @param minPercent lowest accuracy a game needs to count for best pace
     * @return the player's totals on the map
     */
    @Query(TOTALS + " AND g.mapId = :mapId")
    TotalsRow findTotalsOnMap(long userId, long mapId, int minPercent);

    /**
     * Returns the latest finished games on the map, newest first.
     *
     * @param userId the player
     * @param mapId  the map
     * @param limit  most games to return
     * @return the latest finished games on the map, newest first
     */
    @Query(value = RECENT + "AND ug.map_id = :mapId " + NEWEST_FIRST, nativeQuery = true)
    List<RecentGameRow> findRecentFinishedOnMap(long userId, long mapId, int limit);

    /**
     * Returns all finished games, oldest first.
     *
     * @param userId the player
     * @return all finished games, oldest first
     */
    @Query("SELECT g FROM UserGameEntity g WHERE g.userId = :userId AND g.finishedAt IS NOT NULL "
            + "ORDER BY g.finishedAt, g.gameId")
    List<UserGameEntity> findFinished(long userId);

    /**
     * Returns the first guess at each target in the game.
     *
     * @param sessionId the quiz session
     * @return the first guess at each target in the game
     */
    @Query(value = FIRST_GUESSES + "WHERE a.game_id = (SELECT game_id FROM user_games WHERE session_id = :sessionId)"
            + FIRST_TRY_ONLY,
            nativeQuery = true)
    List<FirstGuessRow> findFirstGuesses(String sessionId);

    /**
     * Returns the first guess at each target across those games.
     *
     * @param userId the player
     * @param mapId  the map
     * @param games  how many of the latest games to look at
     * @return the first guess at each target across those games
     */
    @Query(value = FIRST_GUESSES + "JOIN (SELECT game_id FROM user_games WHERE user_id = :userId AND map_id = :mapId "
            + "AND finished_at IS NOT NULL ORDER BY finished_at DESC, game_id DESC LIMIT :games) ug "
            + "ON ug.game_id = a.game_id" + FIRST_TRY_ONLY,
            nativeQuery = true)
    List<FirstGuessRow> findRecentFirstGuesses(long userId, long mapId, int games);

    /**
     * Returns the game on the player's leaderboard row for the map, or empty.
     *
     * @param userId the player
     * @param mapId  the map
     * @return the game on the player's leaderboard row for the map, or empty
     */
    @Query(value = "SELECT g.* FROM user_games g JOIN leaderboard l ON l.game_id = g.game_id "
            + "WHERE l.user_id = :userId AND l.map_id = :mapId",
            nativeQuery = true)
    Optional<UserGameEntity> findBestGame(long userId, long mapId);

    /**
     * Saves a game as the player's leaderboard row for its map, replacing the old one.
     *
     * @param sessionId the quiz session
     * @param accuracy  the game's accuracy percentage
     */
    @Modifying
    @Transactional
    @Query(value = "REPLACE INTO leaderboard (map_id, user_id, game_id, accuracy, duration, achieved_at) "
            + "SELECT map_id, user_id, game_id, CAST(:accuracy AS DECIMAL(5,2)), duration, finished_at "
            + "FROM user_games WHERE session_id = :sessionId",
            nativeQuery = true)
    void saveBestGame(String sessionId, double accuracy);

    /**
     * Returns the player's trophies, oldest first.
     *
     * @param userId the player
     * @return the player's trophies, oldest first
     */
    @Query(value = "SELECT trophy_type AS trophyType, awarded_at AS awardedAt FROM user_trophies "
            + "WHERE user_id = :userId ORDER BY awarded_at",
            nativeQuery = true)
    List<TrophyRow> findTrophies(long userId);

    /**
     * Saves an earned trophy.
     *
     * @param userId     the player
     * @param trophyType the trophy's enum name
     * @param awardedAt  when it was earned
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_trophies (user_id, trophy_type, awarded_at) "
            + "VALUES (:userId, :trophyType, :awardedAt)",
            nativeQuery = true)
    void addTrophy(long userId, String trophyType, LocalDateTime awardedAt);

    /**
     * Returns the player's best on each map with its place, sorted by map name.
     *
     * @param userId the player
     * @return the player's best on each map with its place, sorted by map name
     */
    @Query(value = "SELECT r.map_id AS mapId, m.map_name AS mapName, g.correct_guesses AS correctGuesses, "
            + "g.incorrect_guesses AS incorrectGuesses, r.duration AS duration, r.place AS place, r.players AS players "
            + "FROM (SELECT l.map_id, l.user_id, l.game_id, l.duration, "
            + "RANK() OVER (PARTITION BY l.map_id ORDER BY l.accuracy DESC, l.duration) AS place, "
            + "COUNT(*) OVER (PARTITION BY l.map_id) AS players FROM leaderboard l "
            + "WHERE l.map_id IN (SELECT map_id FROM leaderboard WHERE user_id = :userId)) r "
            + "JOIN map_data m ON m.map_id = r.map_id JOIN user_games g ON g.game_id = r.game_id "
            + "WHERE r.user_id = :userId ORDER BY m.map_name",
            nativeQuery = true)
    List<MapBestRow> findBestPerMap(long userId);

    /**
     * Ranks players across all maps, scoring points for each map by how many players they beat.
     *
     * @param userId the player
     * @return the player's overall place, or empty if they are not on any leaderboard
     */
    @Query(value = "SELECT s.place AS place, s.players AS players "
            + "FROM (SELECT t.user_id, RANK() OVER (ORDER BY t.points DESC) AS place, COUNT(*) OVER () AS players "
            + "FROM (SELECT r.user_id, SUM(r.players - r.place + 1) AS points "
            + "FROM (SELECT l.user_id, "
            + "RANK() OVER (PARTITION BY l.map_id ORDER BY l.accuracy DESC, l.duration) AS place, "
            + "COUNT(*) OVER (PARTITION BY l.map_id) AS players FROM leaderboard l) r "
            + "GROUP BY r.user_id) t) s "
            + "WHERE s.user_id = :userId",
            nativeQuery = true)
    Optional<OverallRankRow> findOverallRank(long userId);

    /**
     * Returns the map's leaderboard, best first.
     *
     * @param mapId the map
     * @param limit most rows to return
     * @return the map's leaderboard, best first
     */
    @Query(value = "SELECT RANK() OVER (ORDER BY t.accuracy DESC, t.duration) AS place, t.user_id AS userId, "
            + "u.username AS username, t.accuracy AS accuracy, t.duration AS duration "
            + "FROM (SELECT l.user_id, l.accuracy, l.duration FROM leaderboard l WHERE l.map_id = :mapId "
            + "ORDER BY l.accuracy DESC, l.duration LIMIT :limit) t "
            + "JOIN users u ON u.user_id = t.user_id ORDER BY place, u.username",
            nativeQuery = true)
    List<LeaderboardRow> findLeaderboard(long mapId, int limit);

    /**
     * Deletes all of a player's trophies.
     *
     * @param userId the player
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_trophies WHERE user_id = :userId", nativeQuery = true)
    void deleteTrophies(long userId);

    /**
     * Deletes all of a player's games.
     *
     * @param userId the player
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_games WHERE user_id = :userId", nativeQuery = true)
    void deleteGames(long userId);
}
