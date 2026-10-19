package com.anyguessr.anyguessrbackend.gameplay.domain;

import java.util.List;

public interface QuizMapRepository {

    List<RegionId> findRegionIdsByMapId(MapId mapId);
}