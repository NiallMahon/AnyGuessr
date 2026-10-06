package com.anyguessr.anyguessrbackend.gameplay.domain;

public record GuessResult(
        boolean correct,
        boolean correctionRequired,
        RegionId selectedRegionId,
        RegionId targetRegionId,
        int correctAnswers,
        int answeredTargets,
        double accuracy,
        boolean quizComplete
) {
}