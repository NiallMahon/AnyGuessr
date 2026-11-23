// How many targets a player got right and wrong on their first try.
package com.anyguessr.anyguessrbackend.stats.domain;

/**
 * Value object for how many targets a player got right and wrong on their first try.
 *
 * @param correct   targets right on the first try
 * @param incorrect targets wrong on the first try
 */
public record Score(int correct, int incorrect) {

    /**
     * Checks the value is valid.
     *
     * @throws IllegalArgumentException if either count is negative
     */
    public Score {
        if (correct < 0 || incorrect < 0) {
            throw new IllegalArgumentException("Correct and incorrect counts must not be negative");
        }
    }

    /**
     * Returns the number of targets.
     *
     * @return the number of targets
     */
    public int total() {
        return correct + incorrect;
    }

    /**
     * Returns the percentage of targets right on the first try, or 0 if there were none.
     *
     * @return the percentage of targets right on the first try, or 0 if there were none
     */
    public double accuracyPercent() {
        return total() == 0 ? 0.0 : correct * 100.0 / total();
    }
}
