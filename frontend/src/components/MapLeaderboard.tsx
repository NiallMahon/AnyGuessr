// A map's top 100 players, with the logged-in player's row highlighted and a head-to-head with anyone else on it.
import "./ProfileStats.css";
import {useEffect, useState} from "react";
import {
    fetchComparison,
    fetchLeaderboard,
    formatPercent,
    formatTime,
    type LeaderboardEntry,
    type MapBest,
    type MapComparison
} from "../services/statsAPI";
import {Popup} from "./ProfileStats";

function score(best: MapBest | null) {
    return best === null ? "not played" : `${formatPercent(best.accuracyPercent)} · ${formatTime(best.durationSeconds)}`;
}

function verdict({yours, theirs}: MapComparison) {
    if (yours === null || theirs === null) {
        return yours === null ? "Not played yet" : "Only you";
    }
    if (yours.place === theirs.place) {
        return "Tied";
    }
    return yours.place < theirs.place ? "You lead" : "They lead";
}

function PlayerCompare({userId, rival, onClose}: {userId: number; rival: LeaderboardEntry; onClose: () => void}) {
    const [maps, setMaps] = useState<MapComparison[] | null>(null);
    const [failed, setFailed] = useState(false);

    useEffect(() => {
        fetchComparison(userId, rival.userId).then(setMaps).catch(() => setFailed(true));
    }, [userId, rival.userId]);

    return (
        <Popup title={`You vs ${rival.username}`} open onClose={onClose}>
            {failed ? <p className="stats-card-empty">Could not load this comparison.</p>
                : maps === null ? <p className="stats-card-empty">Loading...</p>
                : (
                    <ul className="stats-rows">
                        {maps.map((map) => (
                            <li key={map.mapId}>
                                <div className="stats-row-main">
                                    <strong>{map.mapName}</strong>
                                    <span>You: {score(map.yours)}</span>
                                    <span>{rival.username}: {score(map.theirs)}</span>
                                </div>
                                <span>{verdict(map)}</span>
                            </li>
                        ))}
                    </ul>
                )}
        </Popup>
    );
}

function MapLeaderboard({mapId, mapName, available}: {mapId: number; mapName: string; available: boolean}) {
    const [entries, setEntries] = useState<LeaderboardEntry[] | null>(available ? null : []);
    const [failed, setFailed] = useState(false);
    const [rival, setRival] = useState<LeaderboardEntry | null>(null);
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
                                {userId > 0 && entry.userId !== userId ? (
                                    <button type="button" className="stats-link stats-row-link stats-row-main"
                                            title={`Compare with ${entry.username}`} onClick={() => setRival(entry)}>
                                        {entry.username}
                                    </button>
                                ) : <strong className="stats-row-main">{entry.username}</strong>}
                                <span>{formatPercent(entry.accuracyPercent)} · {formatTime(entry.durationSeconds)}</span>
                            </li>
                        ))}
                    </ol>
                )}
            {rival && <PlayerCompare userId={userId} rival={rival} onClose={() => setRival(null)}/>}
        </div>
    );
}

export default MapLeaderboard;
