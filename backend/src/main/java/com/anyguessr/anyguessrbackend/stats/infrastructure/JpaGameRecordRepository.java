// Saves games, guesses, best scores and trophies, and works out profile stats and rankings through JPA.
package com.anyguessr.anyguessrbackend.stats.infrastructure;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecord;
import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository;
import com.anyguessr.anyguessrbackend.stats.domain.GameSessionId;
import com.anyguessr.anyguessrbackend.stats.domain.MapId;
import com.anyguessr.anyguessrbackend.stats.domain.Score;
import com.anyguessr.anyguessrbackend.stats.domain.Trophy;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.infrastructure.SpringDataUserGameRepository.FirstGuessRow;
import com.anyguessr.anyguessrbackend.stats.infrastructure.SpringDataUserGameRepository.RecentGameRow;
import com.anyguessr.anyguessrbackend.stats.infrastructure.SpringDataUserGameRepository.TotalsRow;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link GameRecordRepository} backed by MySQL through Spring Data JPA. Maps
 * database rows to and from the stats domain objects.
 */
public class JpaGameRecordRepository implements GameRecordRepository {

    private final SpringDataUserGameRepository userGames;

    /**
     * Creates the repository.
     *
     * @param userGames the Spring Data repository for the stats tables
     * @throws IllegalArgumentException if it is {@code null}
     */
    public JpaGameRecordRepository(SpringDataUserGameRepository userGames) {
        if (userGames == null) {
            throw new IllegalArgumentException("Spring Data repository must not be null");
        }

        this.userGames = userGames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GameRecord> findBySessionId(GameSessionId sessionId) {
        return userGames.findBySessionId(sessionId.value()).map(this::restore);
    }

    /**
     * Saves a new game as a {@code user_games} row.
     *
     * @param gameRecord the game to save
     */
    @Override
    public void add(GameRecord gameRecord) {
        userGames.save(new UserGameEntity(
                gameRecord.getSessionId().value(),
                gameRecord.getUserId().value(),
                gameRecord.getMapId().value(),
                gameRecord.getStartedAt()));
    }

    /**
     * Saves a guess as a {@code guesses} row, numbering it after the game's earlier guesses.
     *
     * @param sessionId the game the guess belongs to
     * @param guess     the guess
     * @param guessedAt when the guess was made
     */
    @Override
    public void addGuess(GameSessionId sessionId, GameRecord.Guess guess, Instant guessedAt) {
        userGames.addGuess(
                sessionId.value(),
                guess.target().value(),
                guess.selected().value(),
                guess.correct(),
                guess.clickedLat(),
                guess.clickedLng(),
                LocalDateTime.ofInstant(guessedAt, ZoneId.systemDefault()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markFinished(GameRecord gameRecord) {
        Score score = gameRecord.getScore();

        userGames.markFinished(
                gameRecord.getSessionId().value(),
                gameRecord.getFinishedAt(),
                gameRecord.durationSeconds(),
                score.correct(),
                score.incorrect());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerTotals findTotalsOnMap(UserId userId, MapId mapId) {
        return toTotals(userGames.findTotalsOnMap(userId.value(), mapId.value(), PlayerTotals.BEST_PACE_MIN_PERCENT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FinishedGame> findRecentFinishedGamesOnMap(UserId userId, MapId mapId, int limit) {
        return toFinishedGames(userGames.findRecentFinishedOnMap(userId.value(), mapId.value(), limit));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GameRecord> findFinishedGames(UserId userId) {
        return userGames.findFinished(userId.value()).stream().map(this::restore).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FirstGuess> findFirstGuesses(GameSessionId sessionId) {
        return toFirstGuesses(userGames.findFirstGuesses(sessionId.value()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FirstGuess> findRecentFirstGuesses(UserId userId, MapId mapId, int games) {
        return toFirstGuesses(userGames.findRecentFirstGuesses(userId.value(), mapId.value(), games));
    }

    /**
     * Finds the player's best game on a map through the {@code leaderboard} table.
     *
     * @param userId the player
     * @param mapId  the map
     * @return the best game, or empty if none
     */
    @Override
    public Optional<GameRecord> findBestGame(UserId userId, MapId mapId) {
        return userGames.findBestGame(userId.value(), mapId.value()).map(this::restore);
    }

    /**
     * Saves a game as the player's {@code leaderboard} row for its map, replacing the old one.
     *
     * @param gameRecord the new best game
     */
    @Override
    public void saveBestGame(GameRecord gameRecord) {
        userGames.saveBestGame(
                gameRecord.getSessionId().value(),
                gameRecord.getScore().accuracyPercent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EarnedTrophy> findTrophies(UserId userId) {
        return userGames.findTrophies(userId.value()).stream()
                .map(row -> new EarnedTrophy(
                        Trophy.valueOf(row.getTrophyType()),
                        row.getAwardedAt().atZone(ZoneId.systemDefault()).toInstant()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTrophies(UserId userId, Set<Trophy> trophies, Instant awardedAt) {
        LocalDateTime awardedAtLocal = LocalDateTime.ofInstant(awardedAt, ZoneId.systemDefault());
        trophies.forEach(trophy -> userGames.addTrophy(userId.value(), trophy.name(), awardedAtLocal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MapBest> findBestPerMap(UserId userId) {
        return userGames.findBestPerMap(userId.value()).stream()
                .map(row -> new MapBest(
                        new MapId(row.getMapId()),
                        row.getMapName(),
                        new Score(row.getCorrectGuesses(), row.getIncorrectGuesses()),
                        row.getDuration(),
                        row.getPlace(),
                        row.getPlayers()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<OverallRank> findOverallRank(UserId userId) {
        return userGames.findOverallRank(userId.value()).map(row -> new OverallRank(row.getPlace(), row.getPlayers()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LeaderboardEntry> findLeaderboard(MapId mapId, int limit) {
        return userGames.findLeaderboard(mapId.value(), limit).stream()
                .map(row -> new LeaderboardEntry(
                        row.getPlace(),
                        new UserId(row.getUserId()),
                        row.getUsername(),
                        row.getAccuracy(),
                        row.getDuration()))
                .toList();
    }

    /**
     * Deletes the player's trophies and games. Guesses and
     * leaderboard rows are removed by the database's cascading deletes.
     *
     * @param userId the player
     */
    @Override
    @Transactional
    public void deleteAllForUser(UserId userId) {
        userGames.deleteTrophies(userId.value());
        userGames.deleteGames(userId.value());
    }

    private PlayerTotals toTotals(TotalsRow totals) {
        return new PlayerTotals(
                totals.getGamesPlayed(),
                new Score(totals.getCorrect(), totals.getIncorrect()),
                totals.getBestPace(),
                totals.getAveragePace());
    }

    private List<FinishedGame> toFinishedGames(List<RecentGameRow> games) {
        return games.stream()
                .map(game -> new FinishedGame(
                        new GameSessionId(game.getSessionId()),
                        game.getMapName(),
                        game.getFinishedAt().atZone(ZoneId.systemDefault()).toInstant(),
                        new Score(game.getCorrectGuesses(), game.getIncorrectGuesses()),
                        game.getDuration()))
                .toList();
    }

    private List<FirstGuess> toFirstGuesses(List<FirstGuessRow> guesses) {
        return guesses.stream()
                .map(guess -> new FirstGuess(guess.getTarget(), guess.getSelected(), guess.getCorrect() == 1))
                .toList();
    }

    private GameRecord restore(UserGameEntity game) {
        return GameRecord.restore(
                new GameSessionId(game.getSessionId()),
                new UserId(game.getUserId()),
                new MapId(game.getMapId()),
                game.getStartedAt(),
                game.getFinishedAt(),
                game.getFinishedAt() == null ? null : new Score(game.getCorrectGuesses(), game.getIncorrectGuesses()));
    }
}
