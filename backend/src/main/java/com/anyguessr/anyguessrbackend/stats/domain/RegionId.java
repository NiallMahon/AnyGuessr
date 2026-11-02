// ID of a region on a map.
package com.anyguessr.anyguessrbackend.stats.domain;

public record RegionId(long value) {

    public RegionId {
        if (value <= 0) {
            throw new IllegalArgumentException("Region ID must be positive");
        }
    }
}
