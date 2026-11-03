// Spring Data queries for the guesses table.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataGuessRepository extends JpaRepository<GuessEntity, GuessEntity.Key> {

    List<GuessEntity> findByGameIdOrderByAttemptNumber(Long gameId);
}
