import "./Friends.css";
import { useState, useEffect } from "react";

type Friend = {
    id: number;
    name: string;
    location: string;
    initials: string;
    gamesPlayed: number;
    status: string;
};

function Friends() {
    const [friends, setFriends] = useState<Friend[]>([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [newFriendName, setNewFriendName] = useState("");
    const [selectedFriend, setSelectedFriend] = useState<Friend | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    // 1. Fetch friends from the backend when the component loads
    useEffect(() => {
        fetch("http://localhost:8080/friends/1")
            .then((res) => res.json())
            .then((data) => {
                setFriends(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Failed to fetch friends from backend:", err);
                setLoading(false);
            });
    }, []);

    // Filter friends based on search query
    const filteredFriends = friends.filter((friend) =>
        friend.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // 2. Add a new friend via backend POST request
    const handleAddFriend = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newFriendName.trim()) return;

        const initials = newFriendName
            .split(" ")
            .map((n) => n[0])
            .join("")
            .toUpperCase()
            .slice(0, 2);

        const newFriendPayload = {
            name: newFriendName.trim(),
            location: "Unknown",
            initials: initials || "U",
            gamesPlayed: 0,
            status: "Online",
        };

        try {
            const response = await fetch("http://localhost:8080/friends", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(newFriendPayload),
            });

            if (response.ok) {
                const savedFriend = await response.json();
                setFriends([...friends, savedFriend]);
                setNewFriendName("");
            }
        } catch (err) {
            console.error("Error adding friend:", err);
        }
    };

    // 3. Delete a friend via backend DELETE request
    const handleDeleteFriend = async (id: number, e: React.MouseEvent) => {
        e.stopPropagation(); // Prevent triggering the profile view click

        try {
            const response = await fetch(`http://localhost:8080/friends/${id}`, {
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

                <form className="friends-add-form" onSubmit={handleAddFriend}>
                    <input
                        type="text"
                        placeholder="Add friend username..."
                        value={newFriendName}
                        onChange={(e) => setNewFriendName(e.target.value)}
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
                                <p><strong>Location:</strong> {selectedFriend.location}</p>
                                <p><strong>Games Played:</strong> {selectedFriend.gamesPlayed}</p>
                                <p><strong>Status:</strong> {selectedFriend.status}</p>
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