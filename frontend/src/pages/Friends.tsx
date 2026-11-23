import "./Friends.css";
import { useState, useEffect } from "react";

type Friend = {
    id: number;
    userId: string;
    name: string;
    username: string;
    location: string;
    initials: string;
    gamesPlayed: number;
    status: string;
    duelLinkId: string;
};

function Friends() {
    const [currentUserId, setCurrentUserId] = useState("user123");
    const [friends, setFriends] = useState<Friend[]>([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [newFriendUsername, setNewFriendUsername] = useState("");
    const [selectedFriend, setSelectedFriend] = useState<Friend | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        fetchFriends();
    }, [currentUserId]);

    const fetchFriends = async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/friends/user/${currentUserId}`);
            if (response.ok) {
                const data = await response.json();
                setFriends(data);
            }
        } catch (err) {
            console.error("Failed to fetch friends from backend:", err);
        } finally {
            setLoading(false);
        }
    };

    const filteredFriends = friends.filter((friend) =>
        friend.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
        friend.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const handleAddFriend = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newFriendUsername.trim()) return;

        const initials = newFriendUsername
            .split(" ")
            .map((n) => n[0])
            .join("")
            .toUpperCase()
            .slice(0, 2);

        const newFriendPayload = {
            userId: currentUserId,
            name: newFriendUsername.trim(),
            username: newFriendUsername.trim(),
            location: "Unknown",
            initials: initials || "U",
            gamesPlayed: 0,
            status: "Online",
            duelLinkId: "duel-" + Math.random().toString(36).substring(2, 9),
        };

        try {
            const response = await fetch("http://localhost:8080/api/friends", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(newFriendPayload),
            });

            if (response.ok) {
                const savedFriend = await response.json();
                setFriends([...friends, savedFriend]);
                setNewFriendUsername("");
            }
        } catch (err) {
            console.error("Error adding friend:", err);
        }
    };

    const handleDeleteFriend = async (id: number, e: React.MouseEvent) => {
        e.stopPropagation();

        try {
            const response = await fetch(`http://localhost:8080/api/friends/${id}`, {
                method: "DELETE",
            });

            if (response.ok) {
                setFriends(friends.filter((friend) => friend.id !== id));
                if (selectedFriend?.id === id) {
                    setSelectedFriend(null);
                }
            }
        } catch (err) {
            console.error("Error deleting friend:", err);
        }
    };

    return (
        <main className="friends">
            <section className="friends-header">
                <div className="friends-details">
                    <h1>Friends Management</h1>
                    <p>Connect, search, and view your gaming network</p>
                </div>

                <div className="user-switcher" style={{ marginBottom: "10px" }}>
                    <label style={{ marginRight: "8px", fontWeight: "bold" }}>Current User ID:</label>
                    <input
                        type="text"
                        value={currentUserId}
                        onChange={(e) => setCurrentUserId(e.target.value)}
                        className="friends-input"
                        style={{ width: "120px", display: "inline-block" }}
                    />
                </div>

                <form className="friends-add-form" onSubmit={handleAddFriend}>
                    <input
                        type="text"
                        placeholder="Add friend username..."
                        value={newFriendUsername}
                        onChange={(e) => setNewFriendUsername(e.target.value)}
                        className="friends-input"
                    />
                    <button className="friends-edit" type="submit">
                        Add Friend
                    </button>
                </form>
            </section>

            <section className="friends-stats">
                <div>Total Friends: {friends.length}</div>
                <div>Online: {friends.filter(f => f.status !== "Offline").length}</div>
            </section>

            <div className="friends-layout">
                <div className="friends-main-content">
                    <section className="friends-list-section">
                        <h2>Your Friends</h2>

                        <div className="friends-search-container">
                            <input
                                type="text"
                                placeholder="Search usernames..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="friends-search-input"
                            />
                        </div>

                        <div className="friends-grid">
                            {loading ? (
                                <p>Loading friends from backend...</p>
                            ) : filteredFriends.length > 0 ? (
                                filteredFriends.map((friend) => (
                                    <div
                                        key={friend.id}
                                        className="friend-card"
                                        onClick={() => setSelectedFriend(friend)}
                                    >
                                        <div className="friends-card-avatar">
                                            <span>{friend.initials}</span>
                                        </div>
                                        <div className="friend-card-info">
                                            <h3>{friend.name}</h3>
                                            <p>{friend.location}</p>
                                            <span className="friend-status">{friend.status}</span>
                                        </div>
                                        <div className="friend-card-actions">
                                            <button
                                                className="friend-delete-btn"
                                                type="button"
                                                onClick={(e) => handleDeleteFriend(friend.id, e)}
                                            >
                                                Remove
                                            </button>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p className="no-friends-msg">No friends found matching "{searchQuery}"</p>
                            )}
                        </div>
                    </section>
                </div>

                <aside className="friends-sidebar">
                    <section className="friends-profile-preview">
                        <h2>Friend Profile Preview</h2>
                        {selectedFriend ? (
                            <div className="preview-details">
                                <div className="preview-avatar"><span>{selectedFriend.initials}</span></div>
                                <h3>{selectedFriend.name}</h3>
                                <p><strong>Username:</strong> {selectedFriend.username}</p>
                                <p><strong>Location:</strong> {selectedFriend.location}</p>
                                <p><strong>Games Played:</strong> {selectedFriend.gamesPlayed}</p>
                                <p><strong>Status:</strong> {selectedFriend.status}</p>
                                <p><strong>Duel Link ID:</strong> {selectedFriend.duelLinkId}</p>
                            </div>
                        ) : (
                            <p className="preview-placeholder">Click on any friend from the list to view their mini-profile details.</p>
                        )}
                    </section>

                    <section className="friends-requests">
                        <h2>Requests</h2>
                        <p className="requests-placeholder">No pending requests.</p>
                    </section>
                </aside>
            </div>
        </main>
    );
}

export default Friends;