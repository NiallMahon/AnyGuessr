// Fetches a player's stats, map detail, game results, comparisons and map leaderboards, deletes a player's stats, formats stat values, and fetches the player's friends.
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
    flawless: boolean;
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
    currentStreakDays: number;
    bestStreakDays: number;
    trophies: Trophy[];
    bestPerMap: MapBest[];
    overallPlace: number | null;
    overallPlayers: number | null;
}

export interface WeakRegion {
    name: string;
    attempts: number;
    correct: number;
    mixedUpWith: string | null;
}

export interface MapBadge {
    title: string;
    description: string;
    earned: boolean;
}

export interface MapDetail {
    gamesPlayed: number;
    accuracyPercent: number;
    bestPaceSeconds: number | null;
    averagePaceSeconds: number | null;
    best: MapBest | null;
    recentGames: RecentGame[];
    weakRegions: WeakRegion[];
    badges: MapBadge[];
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
    streakDays: number;
    newTrophies: string[];
    missedRegions: string[];
}

export interface MapComparison {
    mapId: number;
    mapName: string;
    yours: MapBest | null;
    theirs: MapBest | null;
}

export interface Friend {
    userId: number;
    name: string;
    location: string;
}

// Formats a percentage to one decimal, e.g. 66.7%.
export function formatPercent(percent: number) {
    return `${Math.round(percent * 10) / 10}%`;
}

// Formats seconds as m:ss, or - if there's no time.
export function formatTime(seconds: number | null) {
    if (seconds === null) {
        return "-";
    }
    const whole = Math.round(seconds);
    return `${Math.floor(whole / 60)}:${String(whole % 60).padStart(2, "0")}`;
}

// Formats a date as day and month, e.g. 22 Nov.
export function shortDate(iso: string) {
    return new Date(iso).toLocaleDateString(undefined, {day: "numeric", month: "short"});
}

// Fetches JSON from the stats API, throwing if the request fails.
async function get<T>(path: string): Promise<T> {
    const res = await fetch(`${BASE}${path}`);
    if (!res.ok) throw new Error(`${path} failed: ${res.status}`);
    return (await res.json()) as T;
}

// Fetches a player's profile stats.
export function fetchUserStats(userId: number): Promise<UserStats> {
    return get<UserStats>(`/users/${userId}`);
}

// Fetches a player's stats on one map.
export function fetchMapDetail(userId: number, mapId: number): Promise<MapDetail> {
    return get<MapDetail>(`/users/${userId}/maps/${mapId}`);
}

// Fetches two players' bests on every map either has played.
export function fetchComparison(userId: number, otherId: number): Promise<MapComparison[]> {
    return get<MapComparison[]>(`/users/${userId}/compare/${otherId}`);
}

export async function fetchFriends(userId: number): Promise<Friend[]> {
    const res = await fetch(`/api/friends/user/${userId}`);
    if (!res.ok) throw new Error(`/api/friends/user/${userId} failed: ${res.status}`);
    return (await res.json()) as Friend[];
}

// Fetches a finished quiz's result, or null if it isn't found.
export async function fetchGameResult(sessionId: string): Promise<GameResult | null> {
    const res = await fetch(`${BASE}/games/${sessionId}`);
    return res.ok ? (await res.json()) as GameResult : null;
}

// Fetches a map's top 100 players.
export function fetchLeaderboard(mapId: number): Promise<LeaderboardEntry[]> {
    return get<LeaderboardEntry[]>(`/maps/${mapId}/leaderboard`);
}

// Deletes all of a player's stats.
export async function deleteUserStats(userId: number): Promise<void> {
    const res = await fetch(`${BASE}/users/${userId}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(`Deleting /users/${userId} failed: ${res.status}`);
}
