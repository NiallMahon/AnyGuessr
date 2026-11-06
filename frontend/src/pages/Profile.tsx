// The logged-in player's profile page: header, stats, comments, rank and friends.
import "./Profile.css";
import {useEffect, useState} from "react";
import {fetchUserStats, type UserStats} from "../services/statsAPI";
import ProfileStats from "../components/ProfileStats";

type UserProfile = {
    id: number;
    name: string;
    location: string;
    initials: string;
    gamesPlayed: number;
};

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
        <main className="profile">
            <section className="profile-header">
                    <div className="profile-avatar">
                        <span>{profile?.initials}</span>
                    </div>
                <div className="profile-details">
                    <h1>{profile?.name}</h1>
                    <p>{profile?.location}</p>
                </div>
                <button className="profile-edit" type="button">
                Edit Profile
                </button>
            </section>
            {stats ? (
                <ProfileStats stats={stats} userId={Number(userId)} onStatsDeleted={() => fetchUserStats(Number(userId)).then(setStats)}/>
            ) : (
                <p className="profile-stats-status">{statsError ?? "Loading stats..."}</p>
            )}
            <div className="profile-layout">
                <div className="profile-main-content">
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

