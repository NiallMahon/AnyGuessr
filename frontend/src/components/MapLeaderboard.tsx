// Top 100 players for a given map
import "./ProfileStats.css";
import {useEffect, useState} from "react";
import {
    fetchLeaderboard,
    formatPercent,
    formatTime,
    type LeaderboardEntry,
    type MapBest
} from "../services/statsAPI";

// Formats a best game as accuracy and time, or "not played".
export function score(best: MapBest | null) {
    return best === null ? "not played" : `${formatPercent(best.accuracyPercent)} · ${formatTime(best.durationSeconds)}`;
}

// A map's top 100 players.
function MapLeaderboard({mapId, mapName, available}: {mapId: number; mapName: string; available: boolean}) {
    const [entries, setEntries] = useState<LeaderboardEntry[] | null>(available ? null : []);
    const [failed, setFailed] = useState(false);
    const userId = Number(sessionStorage.getItem("userId"));

    useEffect(() => {
        if (available) {
            fetchLeaderboard(mapId).then(setEntries).catch(() => setFailed(true));
        }
    }, [mapId, available]);

    return (
        <div className="map-leaderboard">
            <header className="stats-card-header">
                <h3>{mapName}</h3>
                <span>Top 100</span>
            </header>
            {failed ? <p className="stats-card-empty">Could not load the leaderboard.</p>
                : entries === null ? <p className="stats-card-empty">Loading...</p>
                : entries.length === 0 ? <p className="stats-card-empty">No scores yet</p>
                : (
                    <ol className="stats-rows">
                        {entries.map((entry) => (
                            <li key={entry.userId} className={entry.userId === userId ? "leaderboard-you" : undefined}>
                                <span className="leaderboard-place">#{entry.place}</span>
                                <strong className="stats-row-main">{entry.username}</strong>
                                <span>{formatPercent(entry.accuracyPercent)} · {formatTime(entry.durationSeconds)}</span>
                            </li>
                        ))}
                    </ol>
                )}
        </div>
    );
}

export default MapLeaderboard;
