package com.anyguessr.anyguessrbackend.gameplay.ui;

import com.anyguessr.anyguessrbackend.gameplay.application.SubmitGuessUseCase;
import com.anyguessr.anyguessrbackend.gameplay.domain.GuessResult;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final SubmitGuessUseCase submitGuessUseCase;

    public QuizController(
            SubmitGuessUseCase submitGuessUseCase
    ) {
        this.submitGuessUseCase =
                submitGuessUseCase;
    }

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
                        )
                );

        return GuessResponse.from(result);
    }
}