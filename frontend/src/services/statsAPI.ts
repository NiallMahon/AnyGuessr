// Sends a game's start, guesses (one at a time, in order) and finish to the stats backend.

const BASE = '/api/stats';

export interface GuessPayload {
    targetRegionId: number;
    selectedRegionId: number | null;
    lat: number;
    lng: number;
}

export interface GameSummary {
    correct: number;
    incorrect: number;
    durationSeconds: number;
    accuracyPercent: number;
}

async function post<T>(path: string, body?: unknown): Promise<T> {
    const res = await fetch(`${BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`${path} failed: ${res.status}`);
    const text = await res.text();
    return (text ? JSON.parse(text) : undefined) as T;
}

export function startGame(sessionId: string, userId: number, mapId: number): Promise<void> {
    return post<void>(`/games/${sessionId}`, { userId, mapId });
}

export function recordGuess(sessionId: string, guess: GuessPayload): Promise<void> {
    return post<void>(`/games/${sessionId}/guesses`, guess);
}

export function finishGame(sessionId: string): Promise<GameSummary> {
    return post<GameSummary>(`/games/${sessionId}/finish`);
}
