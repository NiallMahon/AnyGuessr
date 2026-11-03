// Profile stat tiles and a bar chart of accuracy over the player's recent games.
import "./ProfileStats.css";
import type {RecentGame, UserStats} from "../services/statsAPI";

function formatPercent(percent: number) {
    return `${Math.round(percent * 10) / 10}%`;
}

function formatTime(seconds: number | null) {
    if (seconds === null) {
        return "-";
    }
    const whole = Math.round(seconds);
    return `${Math.floor(whole / 60)}:${String(whole % 60).padStart(2, "0")}`;
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

function ProfileStats({stats}: {stats: UserStats}) {
    const games = [...stats.recentGames].reverse();
    const answered = stats.correctGuesses + stats.incorrectGuesses;

    return (
        <section className="stats-panel">
            <h2>Your stats</h2>
            <div className="stat-tiles">
                <StatTile label="Games played" value={stats.gamesPlayed} caption="all maps"/>
                <StatTile label="Accuracy" value={formatPercent(stats.accuracyPercent)}
                          caption={`${stats.correctGuesses} of ${answered} correct`}/>
                <StatTile label="Best time" value={formatTime(stats.bestTimeSeconds)} caption="fastest game"/>
                <StatTile label="Average time" value={formatTime(stats.averageTimeSeconds)} caption="per game"/>
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
        </section>
    );
}

export default ProfileStats;
