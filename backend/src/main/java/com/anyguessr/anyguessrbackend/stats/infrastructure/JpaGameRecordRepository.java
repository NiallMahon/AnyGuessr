// Saves games and works out profile stats through JPA.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.infrastructure.SpringDataUserGameRepository.TotalsRow;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public class JpaGameRecordRepository implements GameRecordRepository {

    private final SpringDataUserGameRepository userGames;

    public JpaGameRecordRepository(SpringDataUserGameRepository userGames) {
        if (userGames == null) {
            throw new IllegalArgumentException("Spring Data repository must not be null");
        }

        this.userGames = userGames;
    }

    @Override
    public Optional<GameRecord> findBySessionId(GameSessionId sessionId) {
        return userGames.findBySessionId(sessionId.value()).map(game -> GameRecord.restore(
                sessionId,
                new UserId(game.getUserId()),
                new MapId(game.getMapId()),
                game.getStartedAt(),
                game.getFinishedAt(),
                game.getFinishedAt() == null ? null : new Score(game.getCorrectGuesses(), game.getIncorrectGuesses())));
    }

    @Override
    public void add(GameRecord gameRecord) {
        userGames.save(new UserGameEntity(
                gameRecord.getSessionId().value(),
                gameRecord.getUserId().value(),
                gameRecord.getMapId().value(),
                gameRecord.getStartedAt()));
    }

    @Override
    public void markFinished(GameRecord gameRecord) {
        Score score = gameRecord.getScore().orElseThrow();

        userGames.markFinished(
                gameRecord.getSessionId().value(),
                gameRecord.getFinishedAt().orElseThrow(),
                gameRecord.durationSeconds(),
                score.correct(),
                score.incorrect());
    }

    @Override
    public PlayerTotals findTotalsForUser(UserId userId) {
        TotalsRow totals = userGames.findTotals(userId.value());

        return new PlayerTotals(
                totals.getGamesPlayed(),
                new Score(totals.getCorrect(), totals.getIncorrect()),
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
                        new Score(game.getCorrectGuesses(), game.getIncorrectGuesses()),
                        game.getDuration()))
                .toList();
    }
}
