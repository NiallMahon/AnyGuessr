// A finished quiz: score against your best, map place, streak, new trophies and missed regions.
import "./ProfileStats.css";
import {useEffect, useState} from "react";
import {fetchGameResult, type GameResult as Result} from "../services/statsAPI";
import {formatPercent, formatTime} from "./ProfileStats";

const TRIES = 5;
const RETRY_MS = 300;

function headline(result: Result) {
    if (result.previousBestPercent === null) {
        return "First score on this map";
    }
    if (result.newBest) {
        return "New personal best!";
    }
    return result.accuracyPercent === result.previousBestPercent && result.durationSeconds === result.previousBestSeconds
        ? "You matched your best"
        : "Not your best this time";
}

function GameResult({sessionId}: {sessionId: string}) {
    const [result, setResult] = useState<Result | null>(null);

    useEffect(() => {
        let active = true;

        async function load() {
            for (let tries = 0; tries < TRIES && active; tries++) {
                await new Promise((resolve) => setTimeout(resolve, RETRY_MS));
                const found = await fetchGameResult(sessionId).catch(() => null);
                if (found && active) {
                    setResult(found);
                    return;
                }
            }
        }

        void load();
        return () => {
            active = false;
        };
    }, [sessionId]);

    if (result === null) {
        return null;
    }

    return (
        <section className="stats-card game-result">
            <header className="stats-card-header">
                <h3>{headline(result)}</h3>
                <span>{result.place !== null && `#${result.place} of ${result.players}`}</span>
            </header>
            <ul className="stats-rows">
                <li>
                    <span className="stats-row-main">This game</span>
                    <strong>{formatPercent(result.accuracyPercent)} · {formatTime(result.durationSeconds)}</strong>
                </li>
                {result.previousBestPercent !== null && (
                    <li>
                        <span className="stats-row-main">{result.newBest ? "Previous best" : "Your best"}</span>
                        <span>{formatPercent(result.previousBestPercent)} · {formatTime(result.previousBestSeconds)}</span>
                    </li>
                )}
                <li>
                    <span className="stats-row-main">Streak</span>
                    <span>{result.streakWeeks} {result.streakWeeks === 1 ? "week" : "weeks"}</span>
                </li>
                {result.missedRegions.length > 0 && (
                    <li>
                        <span className="stats-row-main">Missed</span>
                        <span>{result.missedRegions.join(", ")}</span>
                    </li>
                )}
            </ul>
            {result.newTrophies.length > 0 && (
                <ul className="trophy-shelf">
                    {result.newTrophies.map((title) => <li key={title} className="trophy-badge">{title}</li>)}
                </ul>
            )}
        </section>
    );
}

export default GameResult;
