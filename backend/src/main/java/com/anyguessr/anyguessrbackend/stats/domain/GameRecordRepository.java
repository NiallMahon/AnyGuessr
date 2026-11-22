// How games, guesses, best scores, trophies and profile stats are saved and looked up.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for saving and looking up games, guesses, best scores, trophies,
 * rankings and leaderboards.
 */
public interface GameRecordRepository {

    /**
     * A player's totals on one map.
     *
     * @param gamesPlayed        number of finished games
     * @param score              all right and wrong first guesses added together
     * @param bestPaceSeconds    fastest seconds per target in a game of at least {@link #BEST_PACE_MIN_PERCENT}% accuracy, or {@code null}
     * @param averagePaceSeconds average seconds per target, or {@code null} if no games
     */
    record PlayerTotals(int gamesPlayed, Score score, Double bestPaceSeconds, Double averagePaceSeconds) {

        /**
         * Lowest accuracy a game needs for its pace to count as the best pace.
         */
        public static final int BEST_PACE_MIN_PERCENT = 50;
    }

    /**
     * The first guess made for a target.
     *
     * @param target   name of the region asked for
     * @param selected name of the region clicked
     * @param correct  whether the first guess was right
     */
    record FirstGuess(String target, String selected, boolean correct) {}

    /**
     * Summary of one finished game, for the recent games list.
     *
     * @param sessionId       the game
     * @param mapName         name of the map played
     * @param finishedAt      when the game finished
     * @param score           the final score
     * @param durationSeconds how long the game took
     */
    record FinishedGame(GameSessionId sessionId, String mapName, Instant finishedAt, Score score, int durationSeconds) {}

    /**
     * A trophy a player has earned.
     *
     * @param trophy    the trophy
     * @param awardedAt when it was earned
     */
    record EarnedTrophy(Trophy trophy, Instant awardedAt) {}

    /**
     * A player's best game on a map and where it places on that map's leaderboard.
     *
     * @param mapId           the map
     * @param mapName         name of the map
     * @param score           score of the best game
     * @param durationSeconds length of the best game
     * @param place           leaderboard place, starting at 1
     * @param players         number of players on the leaderboard
     */
    record MapBest(MapId mapId, String mapName, Score score, int durationSeconds, int place, int players) {}

    /**
     * A player's place across all maps.
     *
     * @param place   overall place, starting at 1
     * @param players number of ranked players
     */
    record OverallRank(int place, int players) {}

    /**
     * One row of a map leaderboard.
     *
     * @param place           leaderboard place, starting at 1
     * @param userId          the player
     * @param username        the player's name
     * @param accuracyPercent accuracy of their best game
     * @param durationSeconds length of their best game
     */
    record LeaderboardEntry(int place, UserId userId, String username, double accuracyPercent, int durationSeconds) {}

    /**
     * Finds a game by its session.
     *
     * @param sessionId the game to find
     * @return the game, or empty if there is none
     */
    Optional<GameRecord> findBySessionId(GameSessionId sessionId);

    /**
     * Saves a new game.
     *
     * @param gameRecord the game to save
     */
    void add(GameRecord gameRecord);

    /**
     * Saves a guess made in a game.
     *
     * @param sessionId the game the guess belongs to
     * @param guess     the guess
     * @param guessedAt when the guess was made
     */
    void addGuess(GameSessionId sessionId, GameRecord.Guess guess, Instant guessedAt);

    /**
     * Saves the finish time and score of a game.
     *
     * @param gameRecord the finished game
     */
    void markFinished(GameRecord gameRecord);

    /**
     * Returns the player's totals on the map.
     *
     * @param userId the player
     * @param mapId  the map
     * @return the player's totals on the map
     */
    PlayerTotals findTotalsOnMap(UserId userId, MapId mapId);

    /**
     * Returns the player's latest finished games on the map, newest first.
     *
     * @param userId the player
     * @param mapId  the map
     * @param limit  most games to return
     * @return the player's latest finished games on the map, newest first
     */
    List<FinishedGame> findRecentFinishedGamesOnMap(UserId userId, MapId mapId, int limit);

    /**
     * Returns all of the player's finished games, oldest first.
     *
     * @param userId the player
     * @return all of the player's finished games, oldest first
     */
    List<GameRecord> findFinishedGames(UserId userId);

    /**
     * Returns the first guess for each target in the game.
     *
     * @param sessionId the game
     * @return the first guess for each target in the game
     */
    List<FirstGuess> findFirstGuesses(GameSessionId sessionId);

    /**
     * Returns the first guess for each target across those games.
     *
     * @param userId the player
     * @param mapId  the map
     * @param games  how many of the latest games to look at
     * @return the first guess for each target across those games
     */
    List<FirstGuess> findRecentFirstGuesses(UserId userId, MapId mapId, int games);

    /**
     * Returns the player's best game on the map, or empty if none.
     *
     * @param userId the player
     * @param mapId  the map
     * @return the player's best game on the map, or empty if none
     */
    Optional<GameRecord> findBestGame(UserId userId, MapId mapId);

    /**
     * Saves a game as the player's best on its map, replacing the old best.
     *
     * @param gameRecord the new best game
     */
    void saveBestGame(GameRecord gameRecord);

    /**
     * Returns the trophies the player has earned.
     *
     * @param userId the player
     * @return the trophies the player has earned
     */
    List<EarnedTrophy> findTrophies(UserId userId);

    /**
     * Saves newly earned trophies.
     *
     * @param userId    the player
     * @param trophies  the trophies earned
     * @param awardedAt when they were earned
     */
    void addTrophies(UserId userId, Set<Trophy> trophies, Instant awardedAt);

    /**
     * Returns the player's best game on each map they have played.
     *
     * @param userId the player
     * @return the player's best game on each map they have played
     */
    List<MapBest> findBestPerMap(UserId userId);

    /**
     * Returns the player's overall place, or empty if they have no finished games.
     *
     * @param userId the player
     * @return the player's overall place, or empty if they have no finished games
     */
    Optional<OverallRank> findOverallRank(UserId userId);

    /**
     * Returns the map's leaderboard, best first.
     *
     * @param mapId the map
     * @param limit most rows to return
     * @return the map's leaderboard, best first
     */
    List<LeaderboardEntry> findLeaderboard(MapId mapId, int limit);

    /**
     * Deletes every game, guess, best score and trophy for a player.
     *
     * @param userId the player
     */
    void deleteAllForUser(UserId userId);
}
