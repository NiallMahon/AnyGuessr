package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "regions")
public class RegionEntity {

    @Id
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "map_id", nullable = false)
    private Long mapId;

    protected RegionEntity() {
    }

    public RegionEntity(
            Long regionId,
            Long mapId
    ) {
        this.regionId = regionId;
        this.mapId = mapId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public Long getMapId() {
        return mapId;
    }
}