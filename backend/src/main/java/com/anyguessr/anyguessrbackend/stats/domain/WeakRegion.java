// A region a player keeps missing on their first try, and the region they most often pick instead.
package com.anyguessr.anyguessrbackend.stats.domain;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.FirstGuess;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record WeakRegion(String name, int attempts, int correct, String mixedUpWith) {

    private static final int MIN_ATTEMPTS = 2;
    private static final int SHOWN = 5;

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
