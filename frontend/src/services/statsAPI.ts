// Fetches a player's stats, map detail, game results, comparisons and map leaderboards, and deletes a player's stats.
const BASE = '/api/stats';

export interface RecentGame {
    sessionId: string;
    mapName: string;
    finishedAt: string;
    correct: number;
    incorrect: number;
    accuracyPercent: number;
    durationSeconds: number;
}

export interface Trophy {
    type: string;
    title: string;
    description: string;
    awardedAt: string | null;
    progress: number;
    goal: number;
}

export interface MapBest {
    mapId: number;
    mapName: string;
    accuracyPercent: number;
    durationSeconds: number;
    place: number;
    players: number;
}

export interface LeaderboardEntry {
    place: number;
    userId: number;
    username: string;
    accuracyPercent: number;
    durationSeconds: number;
}

export interface UserStats {
    gamesPlayed: number;
    correctGuesses: number;
    incorrectGuesses: number;
    accuracyPercent: number;
    bestPaceSeconds: number | null;
    averagePaceSeconds: number | null;
    currentStreakWeeks: number;
    bestStreakWeeks: number;
    recentGames: RecentGame[];
    trophies: Trophy[];
    bestPerMap: MapBest[];
}

export interface WeakRegion {
    name: string;
    attempts: number;
    correct: number;
    mixedUpWith: string | null;
}

export interface MapDetail {
    gamesPlayed: number;
    accuracyPercent: number;
    bestPaceSeconds: number | null;
    averagePaceSeconds: number | null;
    recentGames: RecentGame[];
    weakRegions: WeakRegion[];
}

export interface GameResult {
    mapId: number;
    correct: number;
    incorrect: number;
    accuracyPercent: number;
    durationSeconds: number;
    newBest: boolean;
    previousBestPercent: number | null;
    previousBestSeconds: number | null;
    place: number | null;
    players: number | null;
    streakWeeks: number;
    newTrophies: string[];
    missedRegions: string[];
}

export interface MapComparison {
    mapId: number;
    mapName: string;
    yours: MapBest | null;
    theirs: MapBest | null;
}

export async function fetchUserStats(userId: number): Promise<UserStats> {
    const res = await fetch(`${BASE}/users/${userId}`);
    if (!res.ok) throw new Error(`/users/${userId} failed: ${res.status}`);
    return (await res.json()) as UserStats;
}

export async function fetchMapDetail(userId: number, mapId: number): Promise<MapDetail> {
    const res = await fetch(`${BASE}/users/${userId}/maps/${mapId}`);
    if (!res.ok) throw new Error(`/users/${userId}/maps/${mapId} failed: ${res.status}`);
    return (await res.json()) as MapDetail;
}

export async function fetchComparison(userId: number, otherId: number): Promise<MapComparison[]> {
    const res = await fetch(`${BASE}/users/${userId}/compare/${otherId}`);
    if (!res.ok) throw new Error(`/users/${userId}/compare/${otherId} failed: ${res.status}`);
    return (await res.json()) as MapComparison[];
}

export async function fetchGameResult(sessionId: string): Promise<GameResult | null> {
    const res = await fetch(`${BASE}/games/${sessionId}`);
    return res.ok ? (await res.json()) as GameResult : null;
}

export async function fetchLeaderboard(mapId: number): Promise<LeaderboardEntry[]> {
    const res = await fetch(`${BASE}/maps/${mapId}/leaderboard`);
    if (!res.ok) throw new Error(`/maps/${mapId}/leaderboard failed: ${res.status}`);
    return (await res.json()) as LeaderboardEntry[];
}

export async function deleteUserStats(userId: number): Promise<void> {
    const res = await fetch(`${BASE}/users/${userId}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(`Deleting /users/${userId} failed: ${res.status}`);
}
