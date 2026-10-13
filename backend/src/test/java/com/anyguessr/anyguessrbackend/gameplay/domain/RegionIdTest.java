package com.anyguessr.anyguessrbackend.gameplay.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionIdTest {

    @Test
    void validRegionIdStoresValue() {
        RegionId regionId = new RegionId("FR");

        assertEquals("FR", regionId.value());
    }

    @Test
    void blankRegionIdIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RegionId(" ")
        );
    }

    @Test
    void regionIdsWithSameValueAreEqual() {
        RegionId first = new RegionId("FR");
        RegionId second = new RegionId("FR");

        assertEquals(first, second);
    }
}