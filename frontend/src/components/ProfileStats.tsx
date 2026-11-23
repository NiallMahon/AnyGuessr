// Profile stats pieces: rank and streak, maps played, trophies, stats deletion (GDPR comppliance), and shared badge rows.
import "./ProfileStats.css";
import {useState} from "react";
import {Link} from "react-router-dom";
import {
    deleteUserStats,
    formatPercent,
    formatTime,
    type MapBest,
    type Trophy,
    type UserStats
} from "../services/statsAPI";

// Shows the player's overall rank and daily streak.
export function RankAndStreak({stats}: {stats: UserStats}) {
    const streak = stats.currentStreakDays;

    return (
        <div className="rank-streak">
            <div title="Each map gives you points for your place on its leaderboard, and your total sets this rank">
                <p className="stat-tile-label">Rank</p>
                <p className="stat-tile-value">{stats.overallPlace === null ? "-" : `#${stats.overallPlace}/${stats.overallPlayers}`}</p>
            </div>
            <div>
                <p className="stat-tile-label">Streak</p>
                <p className="stat-tile-value">{streak} {streak === 1 ? "day" : "days"}</p>
                <p className="stat-tile-caption">best {stats.bestStreakDays}</p>
            </div>
        </div>
    );
}

// One trophy or badge tile, highlighted when earned.
export function BadgeRow({title, status, earned, tip}: {title: string; status: string; earned: boolean; tip: string}) {
    return (
        <div className={`badge${earned ? " earned" : ""}`} title={tip}>
            <strong>{title}</strong>
            <span>{status}</span>
        </div>
    );
}

// Returns a trophy's status: Unlocked, Locked or progress like 3 of 10.
function trophyStatus(trophy: Trophy) {
    if (trophy.awardedAt) {
        return "Unlocked";
    }
    return trophy.goal === 1 ? "Locked" : `${trophy.progress} of ${trophy.goal}`;
}

// Card listing every trophy and how many are earned.
export function TrophyShelf({trophies}: {trophies: Trophy[]}) {
    const earned = trophies.filter((trophy) => trophy.awardedAt !== null).length;

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Trophies</h3>
                <span>{earned} / {trophies.length}</span>
            </header>
            <div className="badge-grid trophy-list stats-scroll">
                {trophies.map((trophy) => (
                    <BadgeRow key={trophy.type} title={trophy.title} status={trophyStatus(trophy)}
                              earned={trophy.awardedAt !== null} tip={trophy.description}/>
                ))}
            </div>
        </div>
    );
}

// Searchable list of the maps the player has finished.
export function MapsPlayed({maps}: {maps: MapBest[]}) {
    const [search, setSearch] = useState("");
    const matches = maps.filter((map) => map.mapName.toLowerCase().includes(search.trim().toLowerCase()));

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Maps played</h3>
                <span>{maps.length} {maps.length === 1 ? "map" : "maps"}</span>
            </header>
            {maps.length === 0 ? <p className="stats-card-empty">Finish a quiz to see your maps here.</p> : (
                <>
                    <input
                        type="search"
                        className="stats-search"
                        placeholder="Search your maps"
                        aria-label="Search your maps"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                    {matches.length === 0 ? <p className="stats-card-empty">No maps match that search.</p> : (
                        <ul className="stats-map-rows stats-scroll capped">
                            {matches.map((map) => (
                                <li key={map.mapId}>
                                    <Link to={`/profile/maps/${map.mapId}`} className="stats-map-row">
                                        <span className="stats-row-main">
                                            <strong>{map.mapName}</strong>
                                            <span>#{map.place} of {map.players} · {formatTime(map.durationSeconds)}</span>
                                        </span>
                                        <span className="trophy-badge">Best {formatPercent(map.accuracyPercent)}</span>
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    )}
                </>
            )}
        </div>
    );
}

// Privacy note with a button to delete all your stats.
export function DeleteStats({userId, onStatsDeleted}: {userId: number; onStatsDeleted: () => Promise<void>}) {
    const [error, setError] = useState<string | null>(null);

    // Confirms, deletes the stats and reloads them.
    async function deleteStats() {
        if (!window.confirm("Delete all your games, best scores and trophies? This can't be undone.")) {
            return;
        }
        try {
            await deleteUserStats(userId);
            await onStatsDeleted();
        } catch {
            setError("Something went wrong. Please refresh the page and check your stats.");
        }
    }

    return (
        <footer className="stats-privacy">
            <p>We keep each game's map, score and time, plus each guess and where you clicked. You can delete them at any time.</p>
            <button type="button" className="stats-delete" onClick={deleteStats}>Delete my stats</button>
            {error && <p role="alert">{error}</p>}
        </footer>
    );
}

