// ID of the player a game belongs to.
package com.anyguessr.anyguessrbackend.stats.domain;

public record UserId(long value) {

    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
    }
}
