// How many UTC days in a row a player has finished a game; today can't break a streak until it ends.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * How many UTC days in a row a player has finished at least one game.
 *
 * @param currentDays the streak running now, or 0 if it has ended
 * @param bestDays    the longest streak ever
 */
public record PlayStreak(int currentDays, int bestDays) {

    /**
     * Works out the streak from a player's finished games. Not having played yet
     * today does not break the current streak.
     *
     * @param finishedGames the player's finished games
     * @param now           the current time
     * @return the current and best streak
     */
    public static PlayStreak from(List<GameRecord> finishedGames, Instant now) {
        List<LocalDate> days = finishedGames.stream()
                .map(game -> dayOf(game.getFinishedAt()))
                .distinct()
                .sorted()
                .toList();

        int run = 0;
        int best = 0;
        LocalDate previous = null;

        for (LocalDate day : days) {
            run = day.minusDays(1).equals(previous) ? run + 1 : 1;
            best = Math.max(best, run);
            previous = day;
        }

        boolean alive = previous != null && !previous.isBefore(dayOf(now).minusDays(1));
        return new PlayStreak(alive ? run : 0, best);
    }

    private static LocalDate dayOf(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).toLocalDate();
    }
}
