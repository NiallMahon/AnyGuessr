// REST endpoints for starting a quiz and submitting guesses.
package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizResult;
import com.anyguessr.anyguessrbackend.gameplay.application.StartQuizUseCase;
import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides REST endpoints for starting quizzes and submitting guesses.
 *
 * Delegates gameplay operations to the application use cases.
 */

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final StartQuizUseCase startQuizUseCase;
    private final SubmitGuessUseCase submitGuessUseCase;

    /**
     * Creates the quiz controller with its required use cases.
     *
     * @param startQuizUseCase handles quiz creation
     * @param submitGuessUseCase handles guess submission
    */

    public QuizController(
            StartQuizUseCase startQuizUseCase,
            SubmitGuessUseCase submitGuessUseCase
    ) {
        this.startQuizUseCase =
                startQuizUseCase;

        this.submitGuessUseCase =
                submitGuessUseCase;
    }

    /**
     * Starts a quiz for the requested map and optional user.
     *
     * @param request contains the map ID and optional user ID
     * @return the new session ID, map ID and initial target
    */

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

    /**
     * Submits a region guess for an existing quiz session.
     *
     * @param sessionId identifies the quiz session
     * @param request contains the selected region and optional coordinates
     * @return the result of the guess and updated quiz progress
    */

    @PostMapping("/{sessionId}/guesses")
    public GuessResponse submitGuess(
            @PathVariable String sessionId,
            @RequestBody SubmitGuessRequest request
    ) {
        GuessResult result =
                submitGuessUseCase.execute(
                        new QuizSessionId(sessionId),
                        new RegionId(
                                request.selectedRegionId()
                        ),
                        request.clickedLat(),
                        request.clickedLng()
                );

        return GuessResponse.from(result);
    }
}