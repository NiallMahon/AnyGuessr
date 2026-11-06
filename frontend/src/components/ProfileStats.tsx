// Profile stat tiles, streak, accuracy chart, trophies, best score and detail per map, and deleting your stats.
import "./ProfileStats.css";
import {type ReactNode, useEffect, useRef, useState} from "react";
import {
    deleteUserStats,
    fetchMapDetail,
    type MapBest,
    type MapDetail,
    type RecentGame,
    type Trophy,
    type UserStats
} from "../services/statsAPI";

export function formatPercent(percent: number) {
    return `${Math.round(percent * 10) / 10}%`;
}

export function formatTime(seconds: number | null) {
    if (seconds === null) {
        return "-";
    }
    const whole = Math.round(seconds);
    return `${Math.floor(whole / 60)}:${String(whole % 60).padStart(2, "0")}`;
}

function formatPace(seconds: number | null) {
    return seconds === null ? "-" : `${seconds.toFixed(1)}s`;
}

function shortDate(iso: string) {
    return new Date(iso).toLocaleDateString(undefined, {day: "numeric", month: "short"});
}

function StatTile({label, value, caption}: {label: string; value: string | number; caption: string}) {
    return (
        <div className="stat-tile">
            <p className="stat-tile-label">{label}</p>
            <p className="stat-tile-value">{value}</p>
            <p className="stat-tile-caption">{caption}</p>
        </div>
    );
}

function AccuracyChart({games}: {games: RecentGame[]}) {
    return (
        <div className="accuracy-chart">
            <div className="accuracy-chart-plot">
                {[100, 50, 0].map((tick) => (
                    <div key={tick} className="accuracy-chart-gridline" style={{bottom: `${tick}%`}}>
                        <span>{tick}%</span>
                    </div>
                ))}
                <ol className="accuracy-chart-bars">
                    {games.map((game, index) => (
                        <li
                            key={game.sessionId}
                            tabIndex={0}
                            aria-label={`${game.mapName}, ${shortDate(game.finishedAt)}: ${formatPercent(game.accuracyPercent)} accuracy`}
                        >
                            <div className="accuracy-chart-bar" style={{height: `${game.accuracyPercent}%`}}>
                                {index === games.length - 1 && (
                                    <span className="accuracy-chart-label">{formatPercent(game.accuracyPercent)}</span>
                                )}
                                <div className="accuracy-chart-tooltip" role="tooltip">
                                    <strong>{formatPercent(game.accuracyPercent)}</strong>
                                    <span>{game.mapName} · {shortDate(game.finishedAt)}</span>
                                    <span>{game.correct}/{game.correct + game.incorrect} correct · {formatTime(game.durationSeconds)}</span>
                                </div>
                            </div>
                        </li>
                    ))}
                </ol>
            </div>
            <div className="accuracy-chart-dates">
                <span>{shortDate(games[0].finishedAt)}</span>
                {games.length > 1 && <span>{shortDate(games[games.length - 1].finishedAt)}</span>}
            </div>
        </div>
    );
}

export function Popup({title, open, onClose, children}: {title: string; open: boolean; onClose: () => void; children: ReactNode}) {
    const ref = useRef<HTMLDialogElement>(null);

    useEffect(() => {
        const dialog = ref.current;
        if (open && dialog && !dialog.open) {
            dialog.showModal();
        }
        if (!open && dialog?.open) {
            dialog.close();
        }
    }, [open]);

    return (
        <dialog ref={ref} className="stats-dialog" onClose={onClose}>
            <header className="stats-card-header">
                <h3>{title}</h3>
                <button type="button" className="stats-link" onClick={onClose}>Close</button>
            </header>
            {children}
        </dialog>
    );
}

function TrophyStatus({trophy}: {trophy: Trophy}) {
    if (trophy.awardedAt) {
        return <span>Earned {shortDate(trophy.awardedAt)}</span>;
    }
    if (trophy.goal === 1) {
        return <span>Locked</span>;
    }
    return (
        <span className="trophy-progress">
            {trophy.progress} of {trophy.goal}
            <span className="trophy-progress-bar">
                <span style={{width: `${trophy.progress * 100 / trophy.goal}%`}}/>
            </span>
        </span>
    );
}

function TrophyShelf({trophies}: {trophies: Trophy[]}) {
    const [open, setOpen] = useState(false);
    const earned = trophies.filter((trophy) => trophy.awardedAt !== null);

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Trophies</h3>
                <button type="button" className="stats-link" onClick={() => setOpen(true)}>
                    {earned.length} of {trophies.length} · See all
                </button>
            </header>
            {earned.length > 0 ? (
                <ul className="trophy-shelf">
                    {earned.map((trophy) => (
                        <li key={trophy.type} className="trophy-badge" title={trophy.description}>{trophy.title}</li>
                    ))}
                </ul>
            ) : <p className="stats-card-empty">Finish a quiz to earn your first trophy.</p>}
            <Popup title="All trophies" open={open} onClose={() => setOpen(false)}>
                <ul className="stats-rows">
                    {trophies.map((trophy) => (
                        <li key={trophy.type} className={trophy.awardedAt ? undefined : "trophy-locked"}>
                            <div className="stats-row-main">
                                <strong>{trophy.title}</strong>
                                <span>{trophy.description}</span>
                            </div>
                            <TrophyStatus trophy={trophy}/>
                        </li>
                    ))}
                </ul>
            </Popup>
        </div>
    );
}

function MapDetailPopup({map, userId, onClose}: {map: MapBest; userId: number; onClose: () => void}) {
    const [detail, setDetail] = useState<MapDetail | null>(null);
    const [failed, setFailed] = useState(false);
    const games = detail ? [...detail.recentGames].reverse() : [];

    useEffect(() => {
        fetchMapDetail(userId, map.mapId).then(setDetail).catch(() => setFailed(true));
    }, [userId, map.mapId]);

    return (
        <Popup title={map.mapName} open onClose={onClose}>
            {failed ? <p className="stats-card-empty">Could not load this map's stats.</p>
                : detail === null ? <p className="stats-card-empty">Loading...</p>
                : (
                    <>
                        <p className="stats-card-empty">
                            {detail.gamesPlayed} {detail.gamesPlayed === 1 ? "game" : "games"}
                            {" · "}{formatPercent(detail.accuracyPercent)} accuracy
                            {" · "}best pace {formatPace(detail.bestPaceSeconds)}
                            {" · "}average pace {formatPace(detail.averagePaceSeconds)}
                        </p>
                        {games.length > 0 && <AccuracyChart games={games}/>}
                        <h4 className="map-detail-heading">Weak regions</h4>
                        {detail.weakRegions.length > 0 ? (
                            <ul className="stats-rows">
                                {detail.weakRegions.map((region) => (
                                    <li key={region.name}>
                                        <div className="stats-row-main">
                                            <strong>{region.name}</strong>
                                            {region.mixedUpWith && <span>Often mixed up with {region.mixedUpWith}</span>}
                                        </div>
                                        <span>{region.correct} of {region.attempts} right</span>
                                    </li>
                                ))}
                            </ul>
                        ) : <p className="stats-card-empty">No weak regions yet. Regions you miss will show up here.</p>}
                    </>
                )}
        </Popup>
    );
}

function BestPerMap({maps, userId}: {maps: MapBest[]; userId: number}) {
    const [selected, setSelected] = useState<MapBest | null>(null);

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Best per map</h3>
                <span>{maps.length} {maps.length === 1 ? "map" : "maps"}</span>
            </header>
            {maps.length > 0 ? (
                <ul className="stats-rows">
                    {maps.map((map) => (
                        <li key={map.mapId}>
                            <div className="stats-row-main">
                                <button type="button" className="stats-link stats-row-link" onClick={() => setSelected(map)}>
                                    {map.mapName}
                                </button>
                                <span>{formatPercent(map.accuracyPercent)} · {formatTime(map.durationSeconds)}</span>
                            </div>
                            {map.accuracyPercent === 100 && <span className="trophy-badge">Flawless</span>}
                            <span>#{map.place} of {map.players}</span>
                        </li>
                    ))}
                </ul>
            ) : <p className="stats-card-empty">Finish a quiz to get on a leaderboard.</p>}
            {selected && <MapDetailPopup map={selected} userId={userId} onClose={() => setSelected(null)}/>}
        </div>
    );
}

function DeleteStats({userId, onStatsDeleted}: {userId: number; onStatsDeleted: () => void}) {
    const [error, setError] = useState<string | null>(null);

    async function deleteStats() {
        if (!window.confirm("Delete all your games, best scores and trophies? This can't be undone.")) {
            return;
        }
        try {
            await deleteUserStats(userId);
            onStatsDeleted();
        } catch {
            setError("Could not delete your stats. Please try again.");
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

function ProfileStats({stats, userId, onStatsDeleted}: {stats: UserStats; userId: number; onStatsDeleted: () => void}) {
    const games = [...stats.recentGames].reverse();
    const answered = stats.correctGuesses + stats.incorrectGuesses;

    return (
        <section className="stats-panel">
            <header className="stats-panel-header">
                <h2>Your stats</h2>
                <span>
                    {stats.currentStreakWeeks > 0
                        ? `${stats.currentStreakWeeks}-week streak`
                        : "Play this week to start a streak"}
                    {stats.bestStreakWeeks > 0 && ` · best ${stats.bestStreakWeeks}`}
                </span>
            </header>
            <div className="stat-tiles">
                <StatTile label="Games played" value={stats.gamesPlayed} caption="all maps"/>
                <StatTile label="Accuracy" value={formatPercent(stats.accuracyPercent)}
                          caption={`${stats.correctGuesses} of ${answered} correct`}/>
                <StatTile label="Best pace" value={formatPace(stats.bestPaceSeconds)} caption="per target, 50%+ right"/>
                <StatTile label="Average pace" value={formatPace(stats.averagePaceSeconds)} caption="per target"/>
            </div>
            <div className="stats-card">
                <header className="stats-card-header">
                    <h3>Accuracy by game</h3>
                    <span>{games.length} {games.length === 1 ? "game" : "games"}</span>
                </header>
                {games.length > 0
                    ? <AccuracyChart games={games}/>
                    : <p className="stats-card-empty">Finish a quiz to see your accuracy here.</p>}
            </div>
            <div className="stats-card-pair">
                <TrophyShelf trophies={stats.trophies}/>
                <BestPerMap maps={stats.bestPerMap} userId={userId}/>
            </div>
            <DeleteStats userId={userId} onStatsDeleted={onStatsDeleted}/>
        </section>
    );
}

export default ProfileStats;
