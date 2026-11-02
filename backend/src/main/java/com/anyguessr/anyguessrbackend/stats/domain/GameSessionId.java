// ID of a game, the same as the quiz session it was played in.
package com.anyguessr.anyguessrbackend.stats.domain;

public record GameSessionId(String value) {

    private static final int MAX_LENGTH = 36;

    public GameSessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Game session ID must not be blank");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Game session ID must be at most " + MAX_LENGTH + " characters");
        }
    }
}
