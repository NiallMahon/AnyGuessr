// ID of the player a game belongs to.
package com.anyguessr.anyguessrbackend.stats.domain;

/**
 * Value object for the ID of the player a game belongs to.
 *
 * @param value the user ID, must be positive
 */
public record UserId(long value) {

    /**
     * Checks the value is valid.
     *
     * @throws IllegalArgumentException if the value is not positive
     */
    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
    }
}
