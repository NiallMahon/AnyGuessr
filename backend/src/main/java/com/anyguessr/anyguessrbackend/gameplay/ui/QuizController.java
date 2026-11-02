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

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final StartQuizUseCase startQuizUseCase;
    private final SubmitGuessUseCase submitGuessUseCase;

    public QuizController(
            StartQuizUseCase startQuizUseCase,
            SubmitGuessUseCase submitGuessUseCase
    ) {
        this.startQuizUseCase =
                startQuizUseCase;

        this.submitGuessUseCase =
                submitGuessUseCase;
    }

    @PostMapping
    public StartQuizResponse startQuiz(
            @RequestBody StartQuizRequest request
    ) {
        StartQuizResult result =
                startQuizUseCase.execute(
                        new MapId(request.mapId())
                );

        return StartQuizResponse.from(result);
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