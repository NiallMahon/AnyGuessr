package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizResult;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** REST endpoints for starting quizzes and queueing their guesses. */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);
    private static final int TRIES = 3;

    private final StartQuizUseCase startQuizUseCase;
    private final SubmitGuessUseCase submitGuessUseCase;
    private final ExecutorService queue = Executors.newSingleThreadExecutor();

    /** Creates the quiz controller with its required use cases. */
    public QuizController(
            StartQuizUseCase startQuizUseCase,
            SubmitGuessUseCase submitGuessUseCase
    ) {
        this.startQuizUseCase =
                startQuizUseCase;

        this.submitGuessUseCase =
                submitGuessUseCase;
    }

    /** Starts a quiz for the requested map and optional user. */
    @PostMapping
    public StartQuizResponse startQuiz(
            @RequestBody StartQuizRequest request
    ) {
        StartQuizResult result =
                startQuizUseCase.execute(
                        new MapId(request.mapId()),
                        request.userId()
                );

        return StartQuizResponse.from(result);
    }

    /** Queues a region guess for an existing quiz session. */
    @PostMapping("/{sessionId}/guesses")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void submitGuess(
            @PathVariable String sessionId,
            @RequestBody SubmitGuessRequest request
    ) {
        Instant guessedAt = Instant.now();

        enqueue("guess in quiz " + sessionId, () ->
                submitGuessUseCase.execute(
                        new QuizSessionId(sessionId),
                        new RegionId(
                                request.selectedRegionId()
                        ),
                        request.clickedLat(),
                        request.clickedLng(),
                        guessedAt
                )
        );
    }

    /** Scores the guesses still queued, then stops the queue. */
    @Override
    public void close() {
        queue.close();
    }

    private void enqueue(String event, Runnable work) {
        queue.execute(() -> {
            for (int attempt = 1; ; attempt++) {
                try {
                    work.run();
                    return;
                } catch (RuntimeException e) {
                    if (attempt == TRIES) {
                        log.error("Gameplay could not score the {}", event, e);
                        return;
                    }
                }
            }
        });
    }
}