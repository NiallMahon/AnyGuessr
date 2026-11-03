package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.List;

public class JpaQuizMapRepository
        implements QuizMapRepository {

    private final SpringDataRegionRepository regionRepository;

    public JpaQuizMapRepository(
            SpringDataRegionRepository regionRepository
    ) {
        this.regionRepository = regionRepository;
    }

    @Override
    public List<RegionId> findRegionIdsByMapId(
            MapId mapId
    ) {
        Long databaseMapId =
                Long.valueOf(mapId.value());

        return regionRepository
                .findByMapIdOrderByRegionIdAsc(
                        databaseMapId
                )
                .stream()
                .map(region ->
                        new RegionId(
                                region.getRegionId()
                                        .toString()
                        )
                )
                .toList();
    }
}