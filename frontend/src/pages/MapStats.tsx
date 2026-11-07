// One map's stats for the logged-in player: rank and pace tiles, badges, accuracy and time by game, weak regions and friends compared.
import "../components/ProfileStats.css";
import {useEffect, useState} from "react";
import {Link, useParams} from "react-router-dom";
import {score, verdict} from "../components/MapLeaderboard";
import {BadgeRow} from "../components/ProfileStats";
import {
    fetchComparison,
    fetchMapDetail,
    formatPercent,
    formatTime,
    FRIENDS,
    type MapBadge,
    type MapBest,
    type MapComparison,
    type MapDetail,
    type RecentGame,
    shortDate,
    type WeakRegion
} from "../services/statsAPI";

const CHART_WIDTH = 900;
const CHART_HEIGHT = 270;
const CHART_PAD_X = 36;
const CHART_PAD_Y = 22;
const WEAK_ROWS = 8;

function formatPace(seconds: number | null) {
    return seconds === null ? "-" : `${seconds.toFixed(1)}s`;
}

function StatTile({label, value, caption}: {label: string; value: string; caption: string}) {
    return (
        <div className="stat-tile">
            <p className="stat-tile-label">{label}</p>
            <p className="stat-tile-value">{value}</p>
            <p className="stat-tile-caption">{caption}</p>
        </div>
    );
}

function chartX(index: number, count: number) {
    const span = CHART_WIDTH - 2 * CHART_PAD_X;
    return CHART_PAD_X + (count === 1 ? span / 2 : index * span / (count - 1));
}

function chartY(fraction: number) {
    return CHART_HEIGHT - CHART_PAD_Y - fraction * (CHART_HEIGHT - 2 * CHART_PAD_Y);
}

function Badges({badges}: {badges: MapBadge[]}) {
    const earned = badges.filter((badge) => badge.earned).length;

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Badges</h3>
                <span>{earned} / {badges.length}</span>
            </header>
            <div className="badge-grid">
                {badges.map((badge) => (
                    <BadgeRow key={badge.title} title={badge.title} status={badge.earned ? "Unlocked" : "Locked"}
                              earned={badge.earned} tip={badge.earned ? `${badge.description}. Earned!` : badge.description}/>
                ))}
            </div>
        </div>
    );
}

function ChartLine({name, fractions, tips}: {name: string; fractions: number[]; tips: string[]}) {
    const points = fractions.map((fraction, index) => [chartX(index, fractions.length), chartY(fraction)]);
    const line = points.map((point) => point.join(",")).join(" ");

    return (
        <g className={`chart-${name}`}>
            <linearGradient id={`fade-${name}`} x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stopOpacity="0.38"/>
                <stop offset="1" stopOpacity="0"/>
            </linearGradient>
            {points.length > 1 && (
                <polygon className="progress-chart-area" fill={`url(#fade-${name})`}
                         points={`${points[0][0]},${chartY(0)} ${line} ${points[points.length - 1][0]},${chartY(0)}`}/>
            )}
            <polyline className="progress-chart-line" points={line}/>
            {points.map(([x, y], index) => (
                <circle key={index} className="progress-chart-point" cx={x} cy={y} r="5"><title>{tips[index]}</title></circle>
            ))}
        </g>
    );
}

function ProgressChart({games, firstGame}: {games: RecentGame[]; firstGame: number}) {
    const slowest = Math.ceil(Math.max(...games.map((game) => game.durationSeconds), 1) / 10) * 10;
    const lastGame = firstGame + games.length - 1;
    const tips = games.map((game, index) => (
        `Game ${firstGame + index} · ${shortDate(game.finishedAt)} · ${formatPercent(game.accuracyPercent)} · ${formatTime(game.durationSeconds)}`
    ));

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <ul className="chart-legend">
                    <li className="chart-accuracy">Accuracy</li>
                    <li className="chart-time">Time taken</li>
                </ul>
                <span>{games.length} {games.length === 1 ? "game" : "games"}</span>
            </header>
            <svg className="progress-chart" viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`} role="img"
                 aria-label={`Accuracy and time taken from game ${firstGame} to game ${lastGame}`}>
                {[0, 0.5, 1].map((fraction) => (
                    <g key={fraction}>
                        <line className="progress-chart-grid" x1={CHART_PAD_X} x2={CHART_WIDTH - CHART_PAD_X}
                              y1={chartY(fraction)} y2={chartY(fraction)} strokeDasharray={fraction ? "3 5" : undefined}/>
                        <text x="0" y={chartY(fraction) + 4}>{fraction * 100}%</text>
                        <text x={CHART_WIDTH} y={chartY(fraction) + 4} textAnchor="end">{formatTime(slowest * fraction)}</text>
                    </g>
                ))}
                <ChartLine name="accuracy" fractions={games.map((game) => game.accuracyPercent / 100)} tips={tips}/>
                <ChartLine name="time" fractions={games.map((game) => game.durationSeconds / slowest)} tips={tips}/>
                <text x={CHART_PAD_X} y={CHART_HEIGHT - 2}>Game {firstGame}</text>
                <text x={CHART_WIDTH / 2} y={CHART_HEIGHT - 2} textAnchor="middle">Games played</text>
                {games.length > 1 && <text x={CHART_WIDTH - CHART_PAD_X} y={CHART_HEIGHT - 2} textAnchor="end">Game {lastGame}</text>}
            </svg>
        </div>
    );
}

function WeakRegions({regions}: {regions: WeakRegion[]}) {
    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Weak regions</h3>
            </header>
            {regions.length > 0 ? (
                <ul className="bar-rows chart-time">
                    {regions.slice(0, WEAK_ROWS).map((region) => (
                        <li key={region.name}>
                            <div className="stats-row-main">
                                <span className="stats-tag">{region.name}</span>
                                {region.mixedUpWith && <span>Often mixed up with {region.mixedUpWith}</span>}
                            </div>
                            <span className="stats-bar"><span style={{width: `${region.correct * 100 / region.attempts}%`}}/></span>
                            <strong>{region.correct}/{region.attempts}</strong>
                        </li>
                    ))}
                </ul>
            ) : <p className="stats-card-empty">No weak regions yet. Regions you miss will show up here.</p>}
        </div>
    );
}

function FriendCompare({userId, best}: {userId: number; best: MapBest}) {
    const [rows, setRows] = useState<MapComparison[] | null>(null);
    const [failed, setFailed] = useState(false);

    useEffect(() => {
        Promise.all(FRIENDS.map((friend) => fetchComparison(userId, friend.userId)))
            .then((lists) => setRows(lists.map((maps) => (
                maps.find((map) => map.mapId === best.mapId) ?? {mapId: best.mapId, mapName: best.mapName, yours: best, theirs: null}
            ))))
            .catch(() => setFailed(true));
    }, [userId, best]);

    return (
        <div className="stats-card">
            <header className="stats-card-header">
                <h3>Friends</h3>
                <span>You: {score(best)}</span>
            </header>
            {FRIENDS.length === 0 ? <p className="stats-card-empty">No friends to compare with yet.</p>
                : failed ? <p className="stats-card-empty">Could not load this comparison.</p>
                : rows === null ? <p className="stats-card-empty">Loading...</p>
                : (
                    <ul className="stats-map-rows stats-scroll capped">
                        {FRIENDS.map((friend, index) => (
                            <li key={friend.userId} className="stats-map-row">
                                <span className="stats-row-main">
                                    <strong>{friend.name}</strong>
                                    <span>{score(rows[index].theirs)}</span>
                                </span>
                                <span className="trophy-badge">{verdict(rows[index])}</span>
                            </li>
                        ))}
                    </ul>
                )}
        </div>
    );
}

function MapStats() {
    const {mapId} = useParams();
    const [detail, setDetail] = useState<MapDetail | null>(null);
    const [failed, setFailed] = useState(false);
    const userId = sessionStorage.getItem("userId");

    useEffect(() => {
        if (userId) {
            fetchMapDetail(Number(userId), Number(mapId)).then(setDetail).catch(() => setFailed(true));
        }
    }, [userId, mapId]);

    return (
        <main className="map-stats">
            <Link to="/profile" className="stats-link">← Back to profile</Link>
            {!userId ? <p className="stats-card-empty">Log in to see your stats.</p>
                : failed ? <p className="stats-card-empty">Could not load this map's stats.</p>
                : detail === null ? <p className="stats-card-empty">Loading...</p>
                : detail.best === null ? <p className="stats-card-empty">You haven't finished a game on this map yet.</p>
                : (
                    <>
                        <h1>{detail.best.mapName}</h1>
                        <div className="stat-tiles">
                            <StatTile label="Rank" value={`#${detail.best.place}`}
                                      caption={`of ${detail.best.players} ${detail.best.players === 1 ? "player" : "players"}`}/>
                            <StatTile label="Best pace" value={formatPace(detail.bestPaceSeconds)} caption="per target, 50%+ right"/>
                            <StatTile label="Average pace" value={formatPace(detail.averagePaceSeconds)} caption="per target"/>
                            <StatTile label="Accuracy" value={formatPercent(detail.accuracyPercent)}
                                      caption={`over ${detail.gamesPlayed} ${detail.gamesPlayed === 1 ? "game" : "games"}`}/>
                        </div>
                        <div className="stats-card-pair badges-first">
                            <Badges badges={detail.badges}/>
                            <ProgressChart games={[...detail.recentGames].reverse()}
                                           firstGame={detail.gamesPlayed - detail.recentGames.length + 1}/>
                        </div>
                        <div className="stats-card-pair even">
                            <WeakRegions regions={detail.weakRegions}/>
                            <FriendCompare userId={Number(userId)} best={detail.best}/>
                        </div>
                    </>
                )}
        </main>
    );
}

export default MapStats;
