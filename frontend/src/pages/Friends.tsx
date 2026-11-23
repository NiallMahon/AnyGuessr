import "./Friends.css";
import { useState, useEffect, useCallback } from "react";

type Friend = {
    userId: number;
    name: string;
    username: string;
    location: string;
    initials: string;
    gamesPlayed: number;
    status: string;
    duelLinkId: string;
};

type RequestItem = {
    otherUserId: number;
    otherUsername: string;
    otherName: string;
    otherInitials: string;
    status: string;
};

type UserSearchResult = {
    user: Friend;
    relationship: "NONE" | "FRIEND" | "OUTGOING_PENDING" | "INCOMING_PENDING";
};

const API_BASE = "http://localhost:8080/api/friends";

function Friends() {
    const [currentUserId, setCurrentUserId] = useState("1");
    const [friends, setFriends] = useState<Friend[]>([]);
    const [incomingRequests, setIncomingRequests] = useState<RequestItem[]>([]);
    const [outgoingRequests, setOutgoingRequests] = useState<RequestItem[]>([]);
    const [blockedUsers, setBlockedUsers] = useState<Friend[]>([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [playerLookupQuery, setPlayerLookupQuery] = useState("");
    const [searchResults, setSearchResults] = useState<UserSearchResult[]>([]);
    const [searchingPlayers, setSearchingPlayers] = useState(false);
    const [selectedFriend, setSelectedFriend] = useState<Friend | null>(null);
    const [activeTab, setActiveTab] = useState<"friends" | "discover" | "blocked">("friends");
    const [loading, setLoading] = useState<boolean>(true);
    const [feedback, setFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);

    const parsedUserId = Number(currentUserId.trim());
    const hasValidUserId = Number.isInteger(parsedUserId) && parsedUserId > 0;

    const loadSocialData = useCallback(async (userIdNum: number) => {
        setLoading(true);
        try {
            const [friendsRes, incomingRes, outgoingRes, blockedRes] = await Promise.all([
                fetch(`${API_BASE}/user/${userIdNum}`),
                fetch(`${API_BASE}/requests/incoming/${userIdNum}`),
                fetch(`${API_BASE}/requests/outgoing/${userIdNum}`),
                fetch(`${API_BASE}/blocked/${userIdNum}`),
            ]);

            setFriends(friendsRes.ok ? await friendsRes.json() : []);
            setIncomingRequests(incomingRes.ok ? await incomingRes.json() : []);
            setOutgoingRequests(outgoingRes.ok ? await outgoingRes.json() : []);
            setBlockedUsers(blockedRes.ok ? await blockedRes.json() : []);
        } catch (err) {
            console.error("Failed to fetch friends data:", err);
            setFeedback({ type: "error", message: "Could not reach backend server." });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        setSelectedFriend(null);
        setFeedback(null);
        setSearchResults([]);
        if (!hasValidUserId) {
            setFriends([]);
            setIncomingRequests([]);
            setOutgoingRequests([]);
            setBlockedUsers([]);
            setLoading(false);
            return;
        }
        loadSocialData(parsedUserId);
    }, [currentUserId, hasValidUserId, parsedUserId, loadSocialData]);

    useEffect(() => {
        const trimmed = playerLookupQuery.trim();
        if (!trimmed || !hasValidUserId) {
            setSearchResults([]);
            return;
        }

        const timer = setTimeout(async () => {
            setSearchingPlayers(true);
            try {
                const res = await fetch(
                    `${API_BASE}/search?userId=${parsedUserId}&query=${encodeURIComponent(trimmed)}`
                );
                if (res.ok) {
                    setSearchResults(await res.json());
                }
            } catch (err) {
                console.error("Player search failed:", err);
            } finally {
                setSearchingPlayers(false);
            }
        }, 250);

        return () => clearTimeout(timer);
    }, [playerLookupQuery, hasValidUserId, parsedUserId, friends, incomingRequests, outgoingRequests]);

    const filteredFriends = friends.filter(
        (friend) =>
            friend.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
            friend.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const extractErrorMessage = async (response: Response, fallback: string): Promise<string> => {
        try {
            const data = await response.json();
            if (data && typeof data.message === "string" && data.message.trim()) {
                return data.message;
            }
        } catch {
            const text = await response.text();
            if (text.trim()) {
                return text;
            }
        }
        return fallback;
    };

    const sendRequestToUsername = async (username: string) => {
        const trimmed = username.trim();
        if (!trimmed || !hasValidUserId) return;

        setFeedback(null);
        try {
            const response = await fetch(`${API_BASE}/requests`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    requesterUserId: parsedUserId,
                    targetUsername: trimmed,
                }),
            });

            if (response.ok) {
                const data = await response.json();
                setFeedback({
                    type: "success",
                    message:
                        data.status === "ACCEPTED"
                            ? `You and @${trimmed} are now friends!`
                            : `Friend request sent to @${trimmed}.`,
                });
                await loadSocialData(parsedUserId);
            } else {
                const msg = await extractErrorMessage(
                    response,
                    "Could not send friend request. Check the username."
                );
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error sending friend request:", err);
            setFeedback({ type: "error", message: "Network error sending friend request." });
        }
    };

    const handleDiscoverSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await sendRequestToUsername(playerLookupQuery);
    };

    const handleAcceptRequest = async (requesterUserId: number) => {
        if (!hasValidUserId) return;
        setFeedback(null);
        try {
            const response = await fetch(
                `${API_BASE}/requests/accept?userId=${parsedUserId}&requesterUserId=${requesterUserId}`,
                { method: "POST" }
            );
            if (response.ok) {
                setFeedback({ type: "success", message: "Friend request accepted." });
                await loadSocialData(parsedUserId);
            } else {
                const msg = await extractErrorMessage(response, "Could not accept request.");
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error accepting request:", err);
            setFeedback({ type: "error", message: "Network error accepting request." });
        }
    };

    const handleDeclineRequest = async (requesterUserId: number) => {
        if (!hasValidUserId) return;
        setFeedback(null);
        try {
            const response = await fetch(
                `${API_BASE}/requests/decline?userId=${parsedUserId}&requesterUserId=${requesterUserId}`,
                { method: "POST" }
            );
            if (response.ok) {
                setIncomingRequests((prev) => prev.filter((r) => r.otherUserId !== requesterUserId));
                setFeedback({ type: "success", message: "Friend request declined." });
            } else {
                const msg = await extractErrorMessage(response, "Could not decline request.");
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error declining request:", err);
            setFeedback({ type: "error", message: "Network error declining request." });
        }
    };

    const handleCancelOutgoingRequest = async (recipientUserId: number) => {
        if (!hasValidUserId) return;
        setFeedback(null);
        try {
            const response = await fetch(
                `${API_BASE}/requests/cancel?userId=${parsedUserId}&recipientUserId=${recipientUserId}`,
                { method: "POST" }
            );
            if (response.ok) {
                setOutgoingRequests((prev) => prev.filter((r) => r.otherUserId !== recipientUserId));
                setFeedback({ type: "success", message: "Sent friend request cancelled." });
            } else {
                const msg = await extractErrorMessage(response, "Could not cancel request.");
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error cancelling request:", err);
            setFeedback({ type: "error", message: "Network error cancelling request." });
        }
    };

    const handleRemoveFriend = async (friendUserId: number, e: React.MouseEvent) => {
        e.stopPropagation();
        if (!hasValidUserId) return;
        setFeedback(null);
        try {
            const response = await fetch(
                `${API_BASE}/${parsedUserId}/${friendUserId}`,
                { method: "DELETE" }
            );

            if (response.ok) {
                setFriends((prev) => prev.filter((f) => f.userId !== friendUserId));
                if (selectedFriend?.userId === friendUserId) {
                    setSelectedFriend(null);
                }
                setFeedback({ type: "success", message: "Friend removed." });
            } else {
                const msg = await extractErrorMessage(response, "Could not remove friend.");
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error removing friend:", err);
            setFeedback({ type: "error", message: "Network error removing friend." });
        }
    };

    const handleBlockUser = async (targetUserId: number, e?: React.MouseEvent) => {
        if (e) e.stopPropagation();
        if (!hasValidUserId) return;
        setFeedback(null);
        try {
            const response = await fetch(
                `${API_BASE}/block/${parsedUserId}/${targetUserId}`,
                { method: "POST" }
            );

            if (response.ok) {
                if (selectedFriend?.userId === targetUserId) {
                    setSelectedFriend(null);
                }
                setFeedback({ type: "success", message: "User blocked." });
                await loadSocialData(parsedUserId);
            } else {
                const msg = await extractErrorMessage(response, "Could not block user.");
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error blocking user:", err);
            setFeedback({ type: "error", message: "Network error blocking user." });
        }
    };

    const handleUnblockUser = async (targetUserId: number) => {
        if (!hasValidUserId) return;
        setFeedback(null);
        try {
            const response = await fetch(
                `${API_BASE}/block/${parsedUserId}/${targetUserId}`,
                { method: "DELETE" }
            );

            if (response.ok) {
                setBlockedUsers((prev) => prev.filter((u) => u.userId !== targetUserId));
                setFeedback({ type: "success", message: "User unblocked." });
            } else {
                const msg = await extractErrorMessage(response, "Could not unblock user.");
                setFeedback({ type: "error", message: msg });
            }
        } catch (err) {
            console.error("Error unblocking user:", err);
            setFeedback({ type: "error", message: "Network error unblocking user." });
        }
    };

    return (
        <main className="friends">
            <section className="friends-header">
                <div className="friends-details">
                    <h1>Friends Management</h1>
                    <p>Discover players, manage friend requests, and connect with your network</p>
                </div>

                <div className="user-switcher" style={{ marginBottom: "10px" }}>
                    <label style={{ marginRight: "8px", fontWeight: "bold" }}>Current User ID:</label>
                    <input
                        type="text"
                        value={currentUserId}
                        onChange={(e) => setCurrentUserId(e.target.value)}
                        className="friends-input"
                        style={{ width: "160px", display: "inline-block" }}
                    />
                </div>

                <form className="friends-add-form" onSubmit={handleDiscoverSubmit}>
                    <input
                        type="text"
                        placeholder="Find players by username..."
                        value={playerLookupQuery}
                        onChange={(e) => {
                            setPlayerLookupQuery(e.target.value);
                            if (activeTab !== "discover" && e.target.value.trim()) {
                                setActiveTab("discover");
                            }
                        }}
                        className="friends-input"
                    />
                    <button className="friends-edit" type="submit">
                        Send Request
                    </button>
                </form>

                {feedback && (
                    <p className={`friends-feedback friends-feedback--${feedback.type}`}>
                        {feedback.message}
                    </p>
                )}
            </section>

            <section className="friends-stats">
                <div>Total Friends: {friends.length}</div>
                <div>Online: {friends.filter((f) => f.status !== "Offline").length}</div>
                <div>Incoming Requests: {incomingRequests.length}</div>
                <div>Sent Requests: {outgoingRequests.length}</div>
            </section>

            <div className="friends-tabs" style={{ display: "flex", gap: "8px", marginBottom: "16px" }}>
                <button
                    type="button"
                    className="friends-edit"
                    onClick={() => setActiveTab("friends")}
                >
                    Friends ({friends.length})
                </button>
                <button
                    type="button"
                    className="friends-edit"
                    onClick={() => setActiveTab("discover")}
                >
                    Find Players
                </button>
                <button
                    type="button"
                    className="friends-edit"
                    onClick={() => setActiveTab("blocked")}
                >
                    Blocked ({blockedUsers.length})
                </button>
            </div>

            <div className="friends-layout">
                <div className="friends-main-content">
                    {activeTab === "friends" && (
                        <section className="friends-list-section">
                            <h2>Your Friends</h2>

                            <div className="friends-search-container">
                                <input
                                    type="text"
                                    placeholder="Filter your friends..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="friends-search-input"
                                />
                            </div>

                            <div className="friends-grid">
                                {loading ? (
                                    <p>Loading friends...</p>
                                ) : filteredFriends.length > 0 ? (
                                    filteredFriends.map((friend) => (
                                        <div
                                            key={friend.userId}
                                            className="friend-card"
                                            onClick={() => setSelectedFriend(friend)}
                                        >
                                            <div className="friends-card-avatar">
                                                <span>{friend.initials}</span>
                                            </div>
                                            <div className="friend-card-info">
                                                <h3>{friend.name}</h3>
                                                <p>@{friend.username} · {friend.location}</p>
                                                <span className="friend-status">{friend.status}</span>
                                            </div>
                                            <div className="friend-card-actions">
                                                <button
                                                    className="friend-delete-btn"
                                                    type="button"
                                                    onClick={(e) => handleRemoveFriend(friend.userId, e)}
                                                >
                                                    Remove
                                                </button>
                                                <button
                                                    className="friend-delete-btn"
                                                    type="button"
                                                    onClick={(e) => handleBlockUser(friend.userId, e)}
                                                >
                                                    Block
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <p className="no-friends-msg">No friends found.</p>
                                )}
                            </div>
                        </section>
                    )}

                    {activeTab === "discover" && (
                        <section className="friends-list-section">
                            <h2>Player Directory</h2>
                            <div className="friends-grid">
                                {searchingPlayers ? (
                                    <p>Searching players...</p>
                                ) : searchResults.length > 0 ? (
                                    searchResults.map(({ user, relationship }) => (
                                        <div key={user.userId} className="friend-card">
                                            <div className="friends-card-avatar">
                                                <span>{user.initials}</span>
                                            </div>
                                            <div className="friend-card-info">
                                                <h3>{user.name}</h3>
                                                <p>@{user.username} · {user.location}</p>
                                                <span className="friend-status">{user.status}</span>
                                            </div>
                                            <div className="friend-card-actions">
                                                {relationship === "NONE" && (
                                                    <button
                                                        className="friends-edit"
                                                        type="button"
                                                        onClick={() => sendRequestToUsername(user.username)}
                                                    >
                                                        Add Friend
                                                    </button>
                                                )}
                                                {relationship === "FRIEND" && <span>Friends</span>}
                                                {relationship === "OUTGOING_PENDING" && <span>Request Sent</span>}
                                                {relationship === "INCOMING_PENDING" && <span>Requested You</span>}
                                                <button
                                                    className="friend-delete-btn"
                                                    type="button"
                                                    onClick={() => handleBlockUser(user.userId)}
                                                >
                                                    Block
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <p className="no-friends-msg">
                                        Type a username above to search registered players.
                                    </p>
                                )}
                            </div>
                        </section>
                    )}

                    {activeTab === "blocked" && (
                        <section className="friends-list-section">
                            <h2>Blocked Users</h2>
                            <div className="friends-grid">
                                {blockedUsers.length > 0 ? (
                                    blockedUsers.map((user) => (
                                        <div key={user.userId} className="friend-card">
                                            <div className="friends-card-avatar">
                                                <span>{user.initials}</span>
                                            </div>
                                            <div className="friend-card-info">
                                                <h3>{user.name}</h3>
                                                <p>@{user.username}</p>
                                            </div>
                                            <div className="friend-card-actions">
                                                <button
                                                    className="friends-edit"
                                                    type="button"
                                                    onClick={() => handleUnblockUser(user.userId)}
                                                >
                                                    Unblock
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <p className="no-friends-msg">No blocked users.</p>
                                )}
                            </div>
                        </section>
                    )}
                </div>

                <aside className="friends-sidebar">
                    <section className="friends-profile-preview">
                        <h2>Friend Profile Preview</h2>
                        {selectedFriend ? (
                            <div className="preview-details">
                                <div className="preview-avatar"><span>{selectedFriend.initials}</span></div>
                                <h3>{selectedFriend.name}</h3>
                                <p><strong>Username:</strong> @{selectedFriend.username}</p>
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
                        <h2>Incoming Requests ({incomingRequests.length})</h2>
                        {incomingRequests.length > 0 ? (
                            <ul className="friend-requests-list">
                                {incomingRequests.map((req) => (
                                    <li key={req.otherUserId} className="friend-request-item">
                                        <div>
                                            <strong>{req.otherName}</strong>
                                            <span> (@{req.otherUsername})</span>
                                        </div>
                                        <div className="friend-request-actions">
                                            <button
                                                type="button"
                                                onClick={() => handleAcceptRequest(req.otherUserId)}
                                            >
                                                Accept
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => handleDeclineRequest(req.otherUserId)}
                                            >
                                                Decline
                                            </button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="requests-placeholder">No incoming requests.</p>
                        )}
                    </section>

                    <section className="friends-requests">
                        <h2>Sent Requests ({outgoingRequests.length})</h2>
                        {outgoingRequests.length > 0 ? (
                            <ul className="friend-requests-list">
                                {outgoingRequests.map((req) => (
                                    <li key={req.otherUserId} className="friend-request-item">
                                        <div>
                                            <strong>{req.otherName}</strong>
                                            <span> (@{req.otherUsername})</span>
                                        </div>
                                        <div className="friend-request-actions">
                                            <button
                                                type="button"
                                                onClick={() => handleCancelOutgoingRequest(req.otherUserId)}
                                            >
                                                Cancel
                                            </button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="requests-placeholder">No sent requests pending.</p>
                        )}
                    </section>
                </aside>
            </div>
        </main>
    );
}

export default Friends;