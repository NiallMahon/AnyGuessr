// How many weeks in a row a player has finished a game; the current week can't break a streak until it ends.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public record PlayStreak(int currentWeeks, int bestWeeks) {

    public static PlayStreak from(List<GameRecord> finishedGames, Instant now) {
        List<LocalDate> weeks = finishedGames.stream()
                .map(game -> weekOf(game.getFinishedAt()))
                .distinct()
                .sorted()
                .toList();

        int run = 0;
        int best = 0;
        LocalDate previous = null;

        for (LocalDate week : weeks) {
            run = week.minusWeeks(1).equals(previous) ? run + 1 : 1;
            best = Math.max(best, run);
            previous = week;
        }

        boolean alive = previous != null && !previous.isBefore(weekOf(now).minusWeeks(1));
        return new PlayStreak(alive ? run : 0, best);
    }

    private static LocalDate weekOf(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).toLocalDate().with(DayOfWeek.MONDAY);
    }
}
