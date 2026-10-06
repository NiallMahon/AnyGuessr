package com.anyguessr.anyguessrbackend.gameplay.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class QuizSession {

    private final QuizSessionId sessionId;
    private final List<RegionId> targetRegionIds;
    private final Set<RegionId> completedRegionIds;

    private RegionId currentTarget;
    private int correctAnswers;
    private int answeredTargets;
    private boolean correctionRequired;
    private QuizStatus status;

    /*
     * Normal constructor used by the application.
     * Targets are placed into a random order.
     */
    public QuizSession(
            QuizSessionId sessionId,
            List<RegionId> targetRegionIds
    ) {
        this(sessionId, targetRegionIds, new Random());
    }

    /*
     * Constructor used by tests so that a fixed Random seed
     * can make the target order reproducible.
     */
    QuizSession(
            QuizSessionId sessionId,
            List<RegionId> targetRegionIds,
            Random random
    ) {
        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Quiz session ID must not be null"
            );
        }

        if (targetRegionIds == null || targetRegionIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Quiz must contain at least one target region"
            );
        }

        if (random == null) {
            throw new IllegalArgumentException(
                    "Random generator must not be null"
            );
        }

        this.sessionId = sessionId;

        List<RegionId> shuffledTargets =
                new ArrayList<>(targetRegionIds);

        Collections.shuffle(shuffledTargets, random);

        this.targetRegionIds = List.copyOf(shuffledTargets);
        this.completedRegionIds = new HashSet<>();

        this.currentTarget = this.targetRegionIds.get(0);
        this.correctAnswers = 0;
        this.answeredTargets = 0;
        this.correctionRequired = false;
        this.status = QuizStatus.IN_PROGRESS;
    }

    public RegionId getCurrentTarget() {
        return currentTarget;
    }

    public GuessResult submitGuess(RegionId selectedRegionId) {

        if (selectedRegionId == null) {
            throw new IllegalArgumentException(
                    "Selected region must not be null"
            );
        }
	if (!targetRegionIds.contains(selectedRegionId)) {
    throw new IllegalArgumentException(
            "Selected region does not belong to this quiz"
    );
}

        if (status == QuizStatus.COMPLETE) {
            throw new IllegalStateException(
                    "Quiz is already complete"
            );
        }

        RegionId targetForThisGuess = currentTarget;

        /*
         * The player's first scored attempt was already wrong.
         * They now need to click the correct region to continue.
         *
         * These correction clicks must NOT change accuracy.
         */
        if (correctionRequired) {

            boolean selectedCorrectTarget =
                    selectedRegionId.equals(currentTarget);

            if (selectedCorrectTarget) {

                completedRegionIds.add(currentTarget);
                correctionRequired = false;

                if (completedRegionIds.size() == targetRegionIds.size()) {
                    status = QuizStatus.COMPLETE;
                } else {
                    moveToNextTarget();
                }
            }

            return createResult(
                    selectedCorrectTarget,
                    selectedRegionId,
                    targetForThisGuess
            );
        }

        /*
         * This is the player's one scored first attempt
         * for the current target.
         */
        answeredTargets++;

        boolean correct =
                selectedRegionId.equals(currentTarget);

        if (correct) {

            correctAnswers++;
            completedRegionIds.add(currentTarget);

            if (completedRegionIds.size() == targetRegionIds.size()) {
                status = QuizStatus.COMPLETE;
            } else {
                moveToNextTarget();
            }

        } else {

            /*
             * Accuracy has already been affected by the wrong answer.
             * The player must now click the revealed correct region
             * before they can continue.
             */
            correctionRequired = true;
        }

        return createResult(
                correct,
                selectedRegionId,
                targetForThisGuess
        );
    }

    /*
     * The targets were shuffled when the QuizSession was created.
     * Therefore moving through this list gives a random order
     * without returning completed targets.
     */
    private void moveToNextTarget() {

        for (RegionId regionId : targetRegionIds) {

            if (!completedRegionIds.contains(regionId)) {
                currentTarget = regionId;
                return;
            }
        }
    }

    private GuessResult createResult(
            boolean correct,
            RegionId selectedRegionId,
            RegionId targetRegionId
    ) {

        double accuracy = answeredTargets == 0
                ? 0.0
                : (correctAnswers * 100.0) / answeredTargets;

        return new GuessResult(
                correct,
                correctionRequired,
                selectedRegionId,
                targetRegionId,
                correctAnswers,
                answeredTargets,
                accuracy,
                status == QuizStatus.COMPLETE
        );
    }
}