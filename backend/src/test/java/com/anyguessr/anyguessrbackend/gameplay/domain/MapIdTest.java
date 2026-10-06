package com.anyguessr.anyguessrbackend.gameplay.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapIdTest {

    @Test
    void validMapIdStoresValue() {
        MapId mapId = new MapId("world-map");

        assertEquals("world-map", mapId.value());
    }

    @Test
    void blankMapIdIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MapId(" ")
        );
    }

    @Test
    void mapIdsWithSameValueAreEqual() {
        MapId first = new MapId("world-map");
        MapId second = new MapId("world-map");

        assertEquals(first, second);
    }
}