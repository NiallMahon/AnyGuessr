// A region a player keeps missing on their first try, and the region they most often pick instead.
package com.anyguessr.anyguessrbackend.stats.domain;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FirstGuess;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A region a player keeps getting wrong on the first try, and the region they
 * most often pick instead.
 *
 * @param name        name of the region
 * @param attempts    times the region was asked for
 * @param correct     times it was right on the first try
 * @param mixedUpWith region most often picked instead, or {@code null}
 */
public record WeakRegion(String name, int attempts, int correct, String mixedUpWith) {

    private static final int MIN_ATTEMPTS = 2;
    private static final int SHOWN = 5;

    /**
     * Ranks a player's weakest regions, worst hit rate first. Only regions asked
     * for at least twice and missed at least once are included, and at most 5 are
     * returned.
     *
     * @param guesses first guesses from the player's recent games
     * @return the weakest regions
     */
    public static List<WeakRegion> rank(List<FirstGuess> guesses) {
        return guesses.stream()
                .collect(Collectors.groupingBy(FirstGuess::target))
                .entrySet().stream()
                .filter(region -> region.getValue().size() >= MIN_ATTEMPTS)
                .map(region -> from(region.getKey(), region.getValue()))
                .filter(region -> region.correct() < region.attempts())
                .sorted(Comparator.comparingDouble(WeakRegion::hitRate)
                        .thenComparing(WeakRegion::attempts, Comparator.reverseOrder())
                        .thenComparing(WeakRegion::name))
                .limit(SHOWN)
                .toList();
    }

    private static WeakRegion from(String name, List<FirstGuess> guesses) {
        List<String> picked = guesses.stream().filter(guess -> !guess.correct()).map(FirstGuess::selected).toList();
        Map<String, Long> timesPicked = picked.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String mixedUpWith = picked.stream()
                .max(Comparator.comparing(timesPicked::get))
                .orElse(null);

        return new WeakRegion(name, guesses.size(), guesses.size() - picked.size(), mixedUpWith);
    }

    private double hitRate() {
        return correct / (double) attempts;
    }
}
