package com.anyguessr.anyguessrbackend.gameplay.domain;

public record GuessResult(
        boolean correct,
        boolean correctionRequired,
        RegionId selectedRegionId,
        RegionId targetRegionId,
        RegionId currentTargetRegionId,
        int correctAnswers,
        int answeredTargets,
        double accuracy,
        boolean quizComplete
) {
}