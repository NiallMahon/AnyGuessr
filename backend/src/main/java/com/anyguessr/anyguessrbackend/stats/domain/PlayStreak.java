// How many UTC days in a row a player has finished a game; today can't break a streak until it ends.
package com.anyguessr.anyguessrbackend.stats.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public record PlayStreak(int currentDays, int bestDays) {

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
