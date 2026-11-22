// The logged-in player's profile page: header, stats, comments and friends.
import "./Profile.css";
import {useEffect, useState} from "react";
import {fetchUserStats, FRIENDS, type UserStats} from "../services/statsAPI";
import {
    DeleteStats,
    MapsPlayed,
    RankAndStreak,
    TrophyShelf
} from "../components/ProfileStats";

type UserProfile = {
    id: number;
    name: string;
    location: string;
    initials: string;
    gamesPlayed: number;
};

function FriendsCard() {
    const [search, setSearch] = useState("");
    const matches = FRIENDS.filter((friend) => friend.name.toLowerCase().includes(search.trim().toLowerCase()));

    return (
        <section className="stats-card profile-friends">
            <header className="stats-card-header">
                <h3>Friends</h3>
                <span>{FRIENDS.length}</span>
            </header>
            <input
                type="search"
                className="stats-search"
                placeholder="Search your friends"
                aria-label="Search your friends"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
            />
            {matches.length === 0 ? (
                <p className="stats-card-empty">{FRIENDS.length === 0 ? "No friends yet." : "No friends match that search."}</p>
            ) : (
                <ul className="stats-map-rows stats-scroll">
                    {matches.map((friend) => (
                        <li key={friend.userId} className="stats-map-row">
                            <span className="stats-row-main">
                                <strong>{friend.name}</strong>
                                <span>{friend.location}</span>
                            </span>
                            <span className="trophy-badge">{friend.status}</span>
                        </li>
                    ))}
                </ul>
            )}
        </section>
    );
}

function Profile() {

    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [userId, setUserId] = useState<number | null>(null);
    useEffect(() => {
        async function fetchProfile() {
           try {
            const response = await fetch("http://localhost:8080/profile/me", {
                credentials: "include",
            });

                if (!response.ok) {
                    throw new Error('Failed to fetch profile');
                }
                const data = await response.json();
                setProfile(data);
                setUserId(data.id);
                sessionStorage.setItem("userId", String(data.id));
            } catch {
                setProfile(null);
            }
            finally {
                setLoading(false);
            }
        }
    

        fetchProfile();
    },[]);

    function handleLogout() {
        sessionStorage.removeItem("userId");
        window.location.href = "http://localhost:8080/logout";
    }

    const [stats, setStats] = useState<UserStats | null>(null);
    const [statsError, setStatsError] = useState<string | null>(null);
    useEffect(() => {
        async function fetchStats() {
            if (userId == null) {
                return;
            }

            try {
               const data = await fetchUserStats(Number(userId));
               setStats(data);
            } catch {
                setStatsError("Could not load stats");
            }
        }

        fetchStats();
    }, [userId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if(!profile) {
        return (
            <main>
                <p>Log in to see your profile</p>
                <a href="/login">Log in</a>
            </main>
        )
    }

    return (
        <main className="profile">
            <section className="profile-header">
                    {stats && <RankAndStreak stats={stats}/>}
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
                <button className="log-out" type="button" onClick={handleLogout}>
                    Log Out
                </button>
            </section>
            <div className="stats-panel">
                <h2>Your stats</h2>
                {!stats && <p>{statsError ?? "Loading stats..."}</p>}
                <div className="profile-grid">
                    <div className="profile-column">
                        {stats && <MapsPlayed maps={stats.bestPerMap}/>}
                        <section className="stats-card">
                            <header className="stats-card-header">
                                <h3>Comments</h3>
                            </header>
                            <p>CS.com: This guy is quite litterally the worst player to touch the game</p>
                        </section>
                    </div>
                    <div className="profile-column">
                        {stats && <TrophyShelf trophies={stats.trophies}/>}
                        <FriendsCard/>
                    </div>
                </div>
                {stats && <DeleteStats userId={Number(userId)} onStatsDeleted={() => fetchUserStats(Number(userId)).then(setStats)}/>}
            </div>
        </main>

    );
}

export default Profile;

