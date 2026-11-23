// ID of a game, the same as the quiz session it was played in.
package com.anyguessr.anyguessrbackend.stats.domain;

/**
 * Value object for a game's ID, the same as the quiz session it was played in.
 *
 * @param value the session ID, at most 36 characters
 */
public record GameSessionId(String value) {

    private static final int MAX_LENGTH = 36;

    /**
     * Checks the value is valid.
     *
     * @throws IllegalArgumentException if the value is blank or too long
     */
    public GameSessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Game session ID must not be blank");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Game session ID must be at most " + MAX_LENGTH + " characters");
        }
    }
}
