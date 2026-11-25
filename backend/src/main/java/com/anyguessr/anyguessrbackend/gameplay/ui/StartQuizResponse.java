package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.List;

public record StartQuizResponse(
        String sessionId,
        String mapId,
        String currentTarget,
        List<String> targets
) {

    public static StartQuizResponse from(
            StartQuizResult result
    ) {
        return new StartQuizResponse(
                result.sessionId().value(),
                result.mapId().value(),
                result.currentTarget().value(),
                result.targets().stream()
                        .map(RegionId::value)
                        .toList()
        );
    }
}
