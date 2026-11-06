package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizResult;

public record StartQuizResponse(
        String sessionId,
        String mapId,
        String currentTarget
) {

    public static StartQuizResponse from(
            StartQuizResult result
    ) {
        return new StartQuizResponse(
                result.sessionId().value(),
                result.mapId().value(),
                result.currentTarget().value()
        );
    }
}