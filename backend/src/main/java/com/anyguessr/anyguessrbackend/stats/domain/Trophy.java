// Trophies a player earns from their finished games, each with a goal and how far along they are.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * Trophies a player earns from their finished games;
 * measures the player's progress against a given trophy's prerequisites.
 */
public enum Trophy {

    /**
     * Finish your first game.
     */
    FIRST_GAME("First steps", "Finish your first game", 1, Facts::gamesPlayed),
    /**
     * Get every target right in a game.
     */
    FLAWLESS("Flawless", "Get every target right in a game", 1, Facts::flawlessMaps),
    /**
     * Average 3 seconds or less per target with at least 80% right.
     */
    SPEEDY("Speedy", "Average 3 seconds or less per target with at least 80% right", 1, Facts::speedyGames),
    /**
     * Finish 10 games.
     */
    REGULAR("Regular", "Finish 10 games", 10, Facts::gamesPlayed),
    /**
     * Score 80% or more in 5 games in a row.
     */
    SHARPSHOOTER("Sharpshooter", "Score 80% or more in 5 games in a row", 5, Facts::bestHighScoreRun),
    /**
     * Finish 50 games.
     */
    COMMITTED("Committed", "Finish 50 games", 50, Facts::gamesPlayed),
    /**
     * Finish 100 games.
     */
    VETERAN("Veteran", "Finish 100 games", 100, Facts::gamesPlayed),
    /**
     * Go flawless on 3 different maps.
     */
    CARTOGRAPHER("Cartographer", "Go flawless on 3 different maps", 3, Facts::flawlessMaps),
    /**
     * Keep a 7-day streak.
     */
    ON_A_ROLL("On a roll", "Keep a 7-day streak", 7, Facts::bestStreakDays);

    private static final int MIN_TARGETS = 3;
    private static final double HIGH_SCORE_PERCENT = 80.0;
    private static final double SPEEDY_SECONDS_PER_TARGET = 3.0;

    /**
     * The metrics trophies are measured on for a player.
     *
     * @param gamesPlayed      finished games
     * @param flawlessMaps     different maps with a flawless game
     * @param speedyGames      games with at least 80% right at 3 seconds or less per target
     * @param bestHighScoreRun most games in a row with 80% or more
     * @param bestStreakDays   longest daily play streak
     */
    public record Facts(int gamesPlayed, int flawlessMaps, int speedyGames, int bestHighScoreRun, int bestStreakDays) {

        /**
         * Works out the facts from a player's finished games.
         *
         * @param finishedGames the player's finished games, oldest first
         * @param now           the current time
         * @return the facts
         */
        public static Facts from(List<GameRecord> finishedGames, Instant now) {
            List<GameRecord> counted = finishedGames.stream()
                    .filter(game -> game.getScore().total() >= MIN_TARGETS)
                    .toList();
            int run = 0;
            int bestRun = 0;

            for (GameRecord game : counted) {
                run = accuracy(game) >= HIGH_SCORE_PERCENT ? run + 1 : 0;
                bestRun = Math.max(bestRun, run);
            }

            return new Facts(
                    finishedGames.size(),
                    (int) counted.stream().filter(game -> isFlawless(game.getScore())).map(GameRecord::getMapId).distinct().count(),
                    (int) counted.stream().filter(Facts::isSpeedy).count(),
                    bestRun,
                    PlayStreak.from(finishedGames, now).bestDays());
        }

        private static boolean isSpeedy(GameRecord game) {
            return accuracy(game) >= HIGH_SCORE_PERCENT && game.secondsPerTarget() <= SPEEDY_SECONDS_PER_TARGET;
        }

        private static double accuracy(GameRecord game) {
            return game.getScore().accuracyPercent();
        }
    }

    private final String title;
    private final String description;
    private final int goal;
    private final ToIntFunction<Facts> measure;

    Trophy(String title, String description, int goal, ToIntFunction<Facts> measure) {
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.measure = measure;
    }

    /**
     * Checks whether a score is flawless: every target right, with at least 3 targets.
     *
     * @param score the score to check
     * @return {@code true} if the score is flawless
     */
    public static boolean isFlawless(Score score) {
        return score.total() >= MIN_TARGETS && score.accuracyPercent() == 100.0;
    }

    /**
     * Finds the trophies that have just reached their goal.
     *
     * @param facts       the player's facts
     * @param alreadyHeld trophies the player already has
     * @return trophies earned now that the player did not have before
     */
    public static Set<Trophy> newlyEarned(Facts facts, Set<Trophy> alreadyHeld) {
        Set<Trophy> earned = EnumSet.noneOf(Trophy.class);

        for (Trophy trophy : values()) {
            if (!alreadyHeld.contains(trophy) && trophy.progress(facts) == trophy.goal) {
                earned.add(trophy);
            }
        }

        return earned;
    }

    /**
     * Returns progress toward this trophy, capped at its goal.
     *
     * @param facts the player's facts
     * @return progress toward this trophy, capped at its goal
     */
    public int progress(Facts facts) {
        return Math.min(measure.applyAsInt(facts), goal);
    }

    /**
     * Returns the number needed to earn this trophy.
     *
     * @return the number needed to earn this trophy
     */
    public int getGoal() {
        return goal;
    }

    /**
     * Returns the trophy name shown to players.
     *
     * @return the trophy name shown to players
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns how to earn the trophy.
     *
     * @return how to earn the trophy
     */
    public String getDescription() {
        return description;
    }
}
