// Fetches a player's stats for the profile page.
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

export interface UserStats {
    gamesPlayed: number;
    correctGuesses: number;
    incorrectGuesses: number;
    accuracyPercent: number;
    bestTimeSeconds: number | null;
    averageTimeSeconds: number | null;
    recentGames: RecentGame[];
}

export async function fetchUserStats(userId: number): Promise<UserStats> {
    const res = await fetch(`${BASE}/users/${userId}`);
    if (!res.ok) throw new Error(`/users/${userId} failed: ${res.status}`);
    return (await res.json()) as UserStats;
}
