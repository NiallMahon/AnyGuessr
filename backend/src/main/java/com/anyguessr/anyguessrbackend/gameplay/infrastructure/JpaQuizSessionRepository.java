package com.anyguessr.anyguessrbackend.gameplay.infrastructure;

import com.anyguessr.anyguessrbackend.gameplay.domain.MapId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSession;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionId;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionRepository;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizSessionSnapshot;
import com.anyguessr.anyguessrbackend.gameplay.domain.QuizStatus;
import com.anyguessr.anyguessrbackend.gameplay.domain.RegionId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JpaQuizSessionRepository
        implements QuizSessionRepository {

    private final SpringDataQuizSessionRepository sessionRepository;
    private final SpringDataQuizSessionTargetRepository targetRepository;

    public JpaQuizSessionRepository(
            SpringDataQuizSessionRepository sessionRepository,
            SpringDataQuizSessionTargetRepository targetRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.targetRepository = targetRepository;
    }

    @Override
    public void save(QuizSession quizSession) {

        QuizSessionSnapshot snapshot =
                quizSession.snapshot();

        QuizSessionEntity sessionEntity =
                new QuizSessionEntity(
                        snapshot.sessionId().value(),
                        Long.valueOf(
                                snapshot.mapId().value()
                        ),
                        Long.valueOf(
                                snapshot.currentTarget().value()
                        ),
                        snapshot.correctAnswers(),
                        snapshot.answeredTargets(),
                        snapshot.correctionRequired(),
                        snapshot.status().name()
                );

        sessionRepository.save(sessionEntity);

        /*
         * Replace the stored target list with the current
         * snapshot so target order and completion state
         * remain consistent.
         */
        List<QuizSessionTargetEntity> existingTargets =
                targetRepository
                        .findByIdSessionIdOrderByIdTargetPositionAsc(
                                snapshot.sessionId().value()
                        );

        if (!existingTargets.isEmpty()) {
            targetRepository.deleteAll(existingTargets);
        }

        List<QuizSessionTargetEntity> targetEntities =
                createTargetEntities(snapshot);

        targetRepository.saveAll(targetEntities);
    }

    @Override
    public QuizSession findById(
            QuizSessionId id
    ) {

        QuizSessionEntity sessionEntity =
                sessionRepository
                        .findById(id.value())
                        .orElse(null);

        if (sessionEntity == null) {
            return null;
        }

        List<QuizSessionTargetEntity> targetEntities =
                targetRepository
                        .findByIdSessionIdOrderByIdTargetPositionAsc(
                                id.value()
                        );

        if (targetEntities.isEmpty()) {
            return null;
        }

        List<RegionId> targetRegionIds =
                targetEntities.stream()
                        .map(entity ->
                                new RegionId(
                                        entity.getRegionId()
                                                .toString()
                                )
                        )
                        .toList();

        Set<RegionId> completedRegionIds =
                new HashSet<>();

        for (QuizSessionTargetEntity entity :
                targetEntities) {

            if (entity.isCompleted()) {
                completedRegionIds.add(
                        new RegionId(
                                entity.getRegionId()
                                        .toString()
                        )
                );
            }
        }

        QuizSessionSnapshot snapshot =
                new QuizSessionSnapshot(
                        new QuizSessionId(
                                sessionEntity.getSessionId()
                        ),
                        new MapId(
                                sessionEntity.getMapId()
                                        .toString()
                        ),
                        targetRegionIds,
                        completedRegionIds,
                        new RegionId(
                                sessionEntity
                                        .getCurrentTargetRegionId()
                                        .toString()
                        ),
                        sessionEntity.getCorrectAnswers(),
                        sessionEntity.getAnsweredTargets(),
                        sessionEntity.isCorrectionRequired(),
                        QuizStatus.valueOf(
                                sessionEntity.getStatus()
                        )
                );

        return QuizSession.restore(snapshot);
    }

    private List<QuizSessionTargetEntity>
    createTargetEntities(
            QuizSessionSnapshot snapshot
    ) {

        List<RegionId> targetRegionIds =
                snapshot.targetRegionIds();

        return java.util.stream.IntStream
                .range(
                        0,
                        targetRegionIds.size()
                )
                .mapToObj(position -> {

                    RegionId regionId =
                            targetRegionIds.get(position);

                    boolean completed =
                            snapshot
                                    .completedRegionIds()
                                    .contains(regionId);

                    return new QuizSessionTargetEntity(
                            snapshot.sessionId().value(),
                            position,
                            Long.valueOf(
                                    regionId.value()
                            ),
                            completed
                    );
                })
                .toList();
    }
}