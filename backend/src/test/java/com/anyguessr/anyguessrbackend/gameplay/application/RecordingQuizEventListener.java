package com.anyguessr.anyguessrbackend.gameplay.application;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.ArrayList;
import java.util.List;

/*
 * Test listener that writes down every announcement it hears,
 * in order, as a short line of text.
 */
public class RecordingQuizEventListener implements QuizEventListener {

    private final List<String> events =
            new ArrayList<>();

    public List<String> events() {
        return events;
    }

    @Override
    public void quizStarted(
            QuizSessionId sessionId,
            Long userId,
            MapId mapId
    ) {
        events.add(
                "started " + sessionId.value()
                        + " user=" + userId
                        + " map=" + mapId.value()
        );
    }

    @Override
    public void guessMade(
            QuizSessionId sessionId,
            RegionId target,
            RegionId selected,
            boolean correct
    ) {
        events.add(
                "guess " + sessionId.value()
                        + " target=" + target.value()
                        + " selected=" + selected.value()
                        + " correct=" + correct
        );
    }

    @Override
    public void quizFinished(
            QuizSessionId sessionId
    ) {
        events.add(
                "finished " + sessionId.value()
        );
    }
}
