package com.anyguessr.anyguessrbackend.stats.domain;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.MapBest;

import java.util.function.BiPredicate;

/**
 * Badges for a given map.
 */
public enum MapBadge {

    /**
     * Finish a game on this map.
     */
    FIRST_GAME("First game", "Finish a game on this map", (games, best) -> games >= 1),
    /**
     * Finish 10 games on this map.
     */
    TEN_GAMES("Ten games", "Finish 10 games on this map", (games, best) -> games >= 10),
    /**
     * Get every target right on this map.
     */
    FLAWLESS("Flawless", "Get every target right on this map", (games, best) -> Trophy.isFlawless(best.score())),
    /**
     * Reach the top 3 on this map's leaderboard.
     */
    TOP_THREE("Top 3", "Reach the top 3 on this map's leaderboard", (games, best) -> best.place() <= 3);

    private final String title;
    private final String description;
    private final BiPredicate<Integer, MapBest> rule;

    MapBadge(String title, String description, BiPredicate<Integer, MapBest> rule) {
        this.title = title;
        this.description = description;
        this.rule = rule;
    }

    /**
     * Checks whether the player has earned this badge on a map.
     *
     * @param gamesPlayed finished games on the map
     * @param best        the player's best game on the map, or {@code null}
     * @return {@code true} if the badge is earned
     */
    public boolean isEarned(int gamesPlayed, MapBest best) {
        return best != null && rule.test(gamesPlayed, best);
    }

    /**
     * Returns the badge name shown to players.
     *
     * @return the badge name shown to players
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns how to earn the badge.
     *
     * @return how to earn the badge
     */
    public String getDescription() {
        return description;
    }
}
