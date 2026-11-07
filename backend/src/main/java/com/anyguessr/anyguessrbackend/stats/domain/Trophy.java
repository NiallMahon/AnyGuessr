// Trophies a player earns from their finished games, each with a goal and how far along they are.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

public enum Trophy {

    FIRST_GAME("First steps", "Finish your first game", 1, Facts::gamesPlayed),
    FLAWLESS("Flawless", "Get every target right in a game", 1, Facts::flawlessMaps),
    SPEEDY("Speedy", "Average 3 seconds or less per target with at least 80% right", 1, Facts::speedyGames),
    REGULAR("Regular", "Finish 10 games", 10, Facts::gamesPlayed),
    SHARPSHOOTER("Sharpshooter", "Score 80% or more in 5 games in a row", 5, Facts::bestHighScoreRun),
    COMMITTED("Committed", "Finish 50 games", 50, Facts::gamesPlayed),
    VETERAN("Veteran", "Finish 100 games", 100, Facts::gamesPlayed),
    CARTOGRAPHER("Cartographer", "Go flawless on 3 different maps", 3, Facts::flawlessMaps),
    ON_A_ROLL("On a roll", "Keep a 7-day streak", 7, Facts::bestStreakDays);

    private static final int MIN_TARGETS = 3;
    private static final double HIGH_SCORE_PERCENT = 80.0;
    private static final double SPEEDY_SECONDS_PER_TARGET = 3.0;

    public record Facts(int gamesPlayed, int flawlessMaps, int speedyGames, int bestHighScoreRun, int bestStreakDays) {

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

    public static boolean isFlawless(Score score) {
        return score.total() >= MIN_TARGETS && score.accuracyPercent() == 100.0;
    }

    public static Set<Trophy> newlyEarned(Facts facts, Set<Trophy> alreadyHeld) {
        Set<Trophy> earned = EnumSet.noneOf(Trophy.class);

        for (Trophy trophy : values()) {
            if (!alreadyHeld.contains(trophy) && trophy.progress(facts) == trophy.goal) {
                earned.add(trophy);
            }
        }

        return earned;
    }

    public int progress(Facts facts) {
        return Math.min(measure.applyAsInt(facts), goal);
    }

    public int getGoal() {
        return goal;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
