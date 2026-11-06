// ID of the map a game is played on.
package com.anyguessr.anyguessrbackend.stats.domain;

public record MapId(long value) {

    public MapId {
        if (value <= 0) {
            throw new IllegalArgumentException("Map ID must be positive");
        }
    }
}
