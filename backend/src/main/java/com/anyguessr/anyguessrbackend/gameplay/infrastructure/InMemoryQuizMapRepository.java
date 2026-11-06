package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizMapRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryQuizMapRepository
        implements QuizMapRepository {

    private final Map<MapId, List<RegionId>> regionsByMap =
            new HashMap<>();

    @Override
    public List<RegionId> findRegionIdsByMapId(
            MapId mapId
    ) {
        return regionsByMap.getOrDefault(
                mapId,
                List.of()
        );
    }

    public void saveMap(
            MapId mapId,
            List<RegionId> regionIds
    ) {
        regionsByMap.put(
                mapId,
                List.copyOf(regionIds)
        );
    }
}