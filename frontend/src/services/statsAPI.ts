/*
frontend for game stats. game files call these functions to send data to the backend for
storage/calculating results

recordGameStart   records gameId
recordRegionClick on every click (targetregion, clickedregion, lat/lng of click, timestamp)
recordGameEnd     sends data to calculate final score in backend
*/

const BASE = '/api/stats';

export interface ClickPayload {
    targetRegionId: number;
    selectedRegionId: number | null;
    lat: number;
    lng: number;
}

export interface GameEndResult {
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

export async function recordGameStart(userId: number, mapId: number): Promise<number> {
    const res = await post<{ gameId: number }>('/games', { userId, mapId });
    return res.gameId;
}

export function recordRegionClick(gameId: number, click: ClickPayload, clickedAt: Date = new Date()): Promise<void> {
    return post<void>(`/games/${gameId}/clicks`, { ...click, clickedAt });
}

export function recordGameEnd(gameId: number): Promise<GameEndResult> {
    return post<GameEndResult>(`/games/${gameId}/end`);
}