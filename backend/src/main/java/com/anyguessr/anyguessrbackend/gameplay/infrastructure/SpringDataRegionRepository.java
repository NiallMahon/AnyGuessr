package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRegionRepository
        extends JpaRepository<RegionEntity, Long> {

    List<RegionEntity> findByMapIdOrderByRegionIdAsc(
            Long mapId
    );
}