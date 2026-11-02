package com.anyguessr.anyguessrbackend.gameplay.domain;

import java.util.List;
import java.util.Set;

public record QuizSessionSnapshot(
        QuizSessionId sessionId,
        MapId mapId,
        List<RegionId> targetRegionIds,
        Set<RegionId> completedRegionIds,
        RegionId currentTarget,
        int correctAnswers,
        int answeredTargets,
        boolean correctionRequired,
        QuizStatus status
) {

    public QuizSessionSnapshot {
        targetRegionIds =
                List.copyOf(targetRegionIds);

        completedRegionIds =
                Set.copyOf(completedRegionIds);
    }
}