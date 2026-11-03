package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.application.QuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.RecordingQuizEventListener;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizResult;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameplayConfigTest {

    @Test
    void useCasesUseTheListenerBeanWhenThereIsOne() {

        RecordingQuizEventListener listener =
                new RecordingQuizEventListener();

        StartQuizUseCase useCase =
                startQuizUseCase(
                        new StaticListableBeanFactory(
                                Map.of("statsListener", listener)
                        ).getBeanProvider(QuizEventListener.class)
                );

        StartQuizResult result =
                useCase.execute(new MapId("1"), 7L);

        assertEquals(
                List.of(
                        "started " + result.sessionId().value()
                                + " user=7 map=1"
                ),
                listener.events()
        );
    }

    @Test
    void useCasesWorkWhenThereIsNoListenerBean() {

        StartQuizUseCase useCase =
                startQuizUseCase(
                        new StaticListableBeanFactory()
                                .getBeanProvider(QuizEventListener.class)
                );

        assertNotNull(
                useCase.execute(new MapId("1"), 7L)
        );
    }

    private StartQuizUseCase startQuizUseCase(
            ObjectProvider<QuizEventListener> listeners
    ) {
        return new GameplayConfig().startQuizUseCase(
                new InMemoryQuizSessionRepository(),
                requestedMapId -> List.of(
                        new RegionId("1")
                ),
                listeners
        );
    }
}
