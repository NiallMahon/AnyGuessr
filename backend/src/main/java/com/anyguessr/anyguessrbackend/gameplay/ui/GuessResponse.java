package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;

public record GuessResponse(
        boolean correct,
        boolean correctionRequired,
        String selectedRegionId,
        String targetRegionId,
        int correctAnswers,
        int answeredTargets,
        double accuracy,
        boolean quizComplete
) {

    public static GuessResponse from(
            GuessResult result
    ) {
        return new GuessResponse(
                result.correct(),
                result.correctionRequired(),
                result.selectedRegionId().value(),
                result.targetRegionId().value(),
                result.correctAnswers(),
                result.answeredTargets(),
                result.accuracy(),
                result.quizComplete()
        );
    }
}