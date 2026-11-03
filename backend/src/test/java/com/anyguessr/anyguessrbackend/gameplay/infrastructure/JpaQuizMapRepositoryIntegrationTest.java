package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JpaQuizMapRepositoryIntegrationTest {

    @Autowired
    private SpringDataRegionRepository regionRepository;

    @Test
    void loadsRegionIdsForSelectedMap() {

        regionRepository.saveAll(
                List.of(
                        new RegionEntity(10L, 1L),
                        new RegionEntity(20L, 1L),
                        new RegionEntity(30L, 1L),
                        new RegionEntity(40L, 2L)
                )
        );

        JpaQuizMapRepository repository =
                new JpaQuizMapRepository(
                        regionRepository
                );

        List<RegionId> regions =
                repository.findRegionIdsByMapId(
                        new MapId("1")
                );

        assertEquals(
                List.of(
                        new RegionId("10"),
                        new RegionId("20"),
                        new RegionId("30")
                ),
                regions
        );
    }
}