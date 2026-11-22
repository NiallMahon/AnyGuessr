// ID of the map a game is played on.
package com.anyguessr.anyguessrbackend.stats.domain;

/**
 * Value object for the ID of the map a game is played on.
 *
 * @param value the map ID, must be positive
 */
public record MapId(long value) {

    /**
     * Checks the value is valid.
     *
     * @throws IllegalArgumentException if the value is not positive
     */
    public MapId {
        if (value <= 0) {
            throw new IllegalArgumentException("Map ID must be positive");
        }
    }
}
