// Badges for one map, earned from how often a player has finished it and their best score there.
package com.anyguessr.anyguessrbackend.stats.domain;

import com.anyguessr.anyguessrbackend.stats.domain.GameRecordRepository.MapBest;

import java.util.function.BiPredicate;

public enum MapBadge {

    FIRST_GAME("First game", "Finish a game on this map", (games, best) -> games >= 1),
    TEN_GAMES("Ten games", "Finish 10 games on this map", (games, best) -> games >= 10),
    FLAWLESS("Flawless", "Get every target right on this map", (games, best) -> Trophy.isFlawless(best.score())),
    TOP_THREE("Top 3", "Reach the top 3 on this map's leaderboard", (games, best) -> best.place() <= 3);

    private final String title;
    private final String description;
    private final BiPredicate<Integer, MapBest> rule;

    MapBadge(String title, String description, BiPredicate<Integer, MapBest> rule) {
        this.title = title;
        this.description = description;
        this.rule = rule;
    }

    public boolean isEarned(int gamesPlayed, MapBest best) {
        return best != null && rule.test(gamesPlayed, best);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
