// How many targets a player got right and wrong on their first try.
package com.anyguessr.anyguessrbackend.stats.domain;

public record Score(int correct, int incorrect) {

    public Score {
        if (correct < 0 || incorrect < 0) {
            throw new IllegalArgumentException("Correct and incorrect counts must not be negative");
        }
    }

    public double accuracyPercent() {
        int total = correct + incorrect;
        return total == 0 ? 0.0 : correct * 100.0 / total;
    }
}
