// Saves games and works out profile stats through JPA.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.GameAlreadyFinishedException;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Guess;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecord.Summary;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.RegionId;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.infrastructure.SpringDataUserGameRepository.TotalsRow;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public class JpaGameRecordRepository implements GameRecordRepository {

    private final SpringDataUserGameRepository userGames;
    private final SpringDataGuessRepository guesses;

    public JpaGameRecordRepository(SpringDataUserGameRepository userGames, SpringDataGuessRepository guesses) {
        if (userGames == null || guesses == null) {
            throw new IllegalArgumentException("Spring Data repositories must not be null");
        }

        this.userGames = userGames;
        this.guesses = guesses;
    }

    @Override
    public Optional<GameRecord> findBySessionId(GameSessionId sessionId) {
        return userGames.findBySessionId(sessionId.value()).map(game -> GameRecord.restore(
                sessionId,
                new UserId(game.getUserId()),
                new MapId(game.getMapId()),
                game.getStartedAt(),
                game.getFinishedAt(),
                guesses.findByGameIdOrderByAttemptNumber(game.getGameId()).stream()
                        .map(JpaGameRecordRepository::toGuess)
                        .toList()));
    }

    @Override
    public boolean userExists(UserId userId) {
        return userGames.countUsers(userId.value()) > 0;
    }

    @Override
    public boolean mapExists(MapId mapId) {
        return userGames.countMaps(mapId.value()) > 0;
    }

    @Override
    public boolean regionIsOnMap(RegionId regionId, MapId mapId) {
        return userGames.countRegionsOnMap(regionId.value(), mapId.value()) > 0;
    }

    @Override
    public void add(GameRecord gameRecord) {
        insert(() -> userGames.save(new UserGameEntity(
                gameRecord.getSessionId().value(),
                gameRecord.getUserId().value(),
                gameRecord.getMapId().value(),
                gameRecord.getStartedAt())));
    }

    @Override
    public void addGuess(GameRecord gameRecord, Guess guess) {
        long gameId = userGames.findBySessionId(gameRecord.getSessionId().value()).orElseThrow().getGameId();

        insert(() -> guesses.save(new GuessEntity(
                gameId,
                guess.attemptNumber(),
                gameRecord.getMapId().value(),
                guess.targetRegionId().value(),
                guess.selectedRegionId() == null ? null : guess.selectedRegionId().value(),
                guess.correct(),
                guess.lat(),
                guess.lng(),
                guess.guessedAt())));
    }

    @Override
    public void markFinished(GameRecord gameRecord) {
        Summary summary = gameRecord.summary();

        int updated = userGames.markFinished(
                gameRecord.getSessionId().value(),
                gameRecord.getFinishedAt().orElseThrow(),
                summary.durationSeconds(),
                summary.correctGuesses(),
                summary.incorrectGuesses());

        if (updated == 0) {
            throw new GameAlreadyFinishedException(gameRecord.getSessionId());
        }
    }

    @Override
    public PlayerTotals findTotalsForUser(UserId userId) {
        TotalsRow totals = userGames.findTotals(userId.value());

        return new PlayerTotals(
                totals.getGamesPlayed(),
                totals.getCorrect(),
                totals.getIncorrect(),
                totals.getBestTime(),
                totals.getAverageTime() == null ? null : (int) Math.round(totals.getAverageTime()));
    }

    @Override
    public List<FinishedGame> findRecentFinishedGames(UserId userId, int limit) {
        return userGames.findRecentFinished(userId.value(), limit).stream()
                .map(game -> new FinishedGame(
                        new GameSessionId(game.getSessionId()),
                        game.getMapName(),
                        game.getFinishedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        game.getCorrectGuesses(),
                        game.getIncorrectGuesses(),
                        game.getDuration()))
                .toList();
    }

    // JPA reports a clash with an existing row as a general DataIntegrityViolationException.
    // StatsController answers 409 for DuplicateKeyException (what JdbcTemplate threw), so clashes are turned into that.
    private static void insert(Runnable save) {
        try {
            save.run();
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException clash && clash.getKind() == ConstraintKind.UNIQUE) {
                throw new DuplicateKeyException(e.getMessage(), e);
            }

            throw e;
        }
    }

    private static Guess toGuess(GuessEntity guess) {
        return new Guess(
                guess.getAttemptNumber(),
                new RegionId(guess.getTargetRegionId()),
                guess.getSelectedRegionId() == null ? null : new RegionId(guess.getSelectedRegionId()),
                guess.isCorrect(),
                guess.getLat(),
                guess.getLng(),
                guess.getGuessedAt());
    }
}
