import "./Profile.css";
import {useEffect, useState} from "react";
import {fetchUserStats, type UserStats} from "../services/userStatsAPI";

type UserProfile = {
    id: number;
    name: string;
    location: string;
    initials: string;
    gamesPlayed: number;
};

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

//Simple profile page for the user, with a profile picture, name, location, and stats. Also would include recent games, comments, and friends
function Profile() {

    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const userId = sessionStorage.getItem("userId");
    useEffect(() => {
        async function fetchProfile() {
            if(!userId) {
                setError("User not logged in");
                setLoading(false);
                return;
            }

            try {
                const response = await fetch(`http://localhost:8080/profile/${userId}`);

                if (!response.ok) {
                    throw new Error('Failed to fetch profile');
                }
                const data = await response.json();
                setProfile(data);
            } catch (err) {
                setError(
                    err instanceof Error ? err.message : 'Failed to fetch profile'
                );
            } finally {
                setLoading(false);
            }
        }

        fetchProfile();
    },[userId]);

    const [stats, setStats] = useState<UserStats | null>(null);
    const [statsError, setStatsError] = useState<string | null>(null);
    useEffect(() => {
        async function fetchStats() {
            if (!userId) {
                return;
            }

            try {
                setStats(await fetchUserStats(Number(userId)));
            } catch {
                setStatsError("Could not load stats");
            }
        }

        fetchStats();
    }, [userId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        // Main profile container
        <main className="profile">
            {/* Profile header section with avatar, name, location, and member since date */}
            <section className="profile-header">
                    <div className="profile-avatar">
                        {/* Placeholder for profile picture, currently just initials */}
                        <span>{profile?.initials}</span>
                    </div>
                <div className="profile-details">
                    <h1>{profile?.name}</h1>
                    <p>{profile?.location}</p>
                </div>
                {/* Edit profile button */}
                <button className="profile-edit" type="button">
                Edit Profile
                </button>
            </section>
            {/* Profile stats section with trophies, high score, games played, games won, and win rate */}
            <section className="profile-stats">
                {stats ? (
                    <>
                        <div><span className="profile-stat-value">{stats.gamesPlayed}</span>Games Played</div>
                        <div><span className="profile-stat-value">{stats.correctGuesses}</span>Correct</div>
                        <div><span className="profile-stat-value">{stats.incorrectGuesses}</span>Incorrect</div>
                        <div><span className="profile-stat-value">{formatPercent(stats.accuracyPercent)}</span>Accuracy</div>
                        <div><span className="profile-stat-value">{formatTime(stats.bestTimeSeconds)}</span>Best Time</div>
                        <div><span className="profile-stat-value">{formatTime(stats.averageTimeSeconds)}</span>Average Time</div>
                    </>
                ) : (
                    <div>{statsError ?? "Loading stats..."}</div>
                )}
            </section>
            {/* Profile layout section with recent games, comments in the main page section of the page (75% of width) while rank and friends are in the sidebar */}
            <div className="profile-layout">
                <div className="profile-main-content">
                    <section className="profile-recent-games">
                        <h2>Recent Games</h2>

                            <div className="recent-games-list">
                            {stats?.recentGames.map((game) => (
                                <div className="game-card" key={game.sessionId}>
                                    <h3>{game.mapName}</h3>
                                    <p>{new Date(game.finishedAt).toLocaleDateString()}</p>
                                    <p>Score: {game.correct}/{game.correct + game.incorrect}</p>
                                    <p>Accuracy: {formatPercent(game.accuracyPercent)}</p>
                                    <p>Time: {formatTime(game.durationSeconds)}</p>
                                </div>
                            ))}
                </div>
                {stats?.recentGames.length === 0 && <p>No games yet</p>}
            </section>
            
            <section className="profile-comments">
                <h2>Comments</h2>
                <p>CS.com: This guy is quite litterally the worst player to touch the game</p>
            </section>
            </div>
            
            <aside className="profile-sidebar">
                <section className='profile-rank'>
                    <h2>Rank</h2>
                </section>

                <section className="profile-friends">
                    <h2>Friends</h2>
                </section>
            </aside>
            </div>
        </main>

    );
}

export default Profile;

