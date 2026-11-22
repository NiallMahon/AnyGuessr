// ID of a region on a map.
package com.anyguessr.anyguessrbackend.stats.domain;

/**
 * Value object for the ID of a region on a map.
 *
 * @param value the region ID, must be positive
 */
public record RegionId(long value) {

    /**
     * Checks the value is valid.
     *
     * @throws IllegalArgumentException if the value is not positive
     */
    public RegionId {
        if (value <= 0) {
            throw new IllegalArgumentException("Region ID must be positive");
        }
    }
}
