import React, {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState
} from "react";

import "./Friends.css";


interface UserSummary {
    userId: number;
    name: string;
    username: string;
    location: string;
    initials: string;
    gamesPlayed: number;
    status: string;
    duelLinkId: string;
}

interface RequestView {
    otherUserId: number;
    otherUsername: string;
    otherName: string;
    otherInitials: string;
    status: string;
}

interface UserSearchResult {
    user: UserSummary;
    relationship: string;
}

interface UsernameOption {
    id: number;
    username: string;
    label: string;
    relationship: string;
    isFriend: boolean;
    friend?: UserSummary;
}



const API_BASE = "http://localhost:8080/api/friends";
const PROFILE_ME_URL = "http://localhost:8080/profile/me";


/**
 * Resolves the signed-in user's ID.
 */
async function resolveSignedInUserId(): Promise<number | null> {

    const storedId = sessionStorage.getItem("userId");

    if (storedId && storedId.trim() !== "" && Number.isFinite(Number(storedId))) {
        return Number(storedId);
    }

    try {
        const res = await fetch(PROFILE_ME_URL, {
            credentials: "include"
        });

        if (!res.ok) return null;

        const data = await res.json();

        const id = data?.id ?? data?.userId;

        if (typeof id === "number" && Number.isFinite(id)) {
            sessionStorage.setItem("userId", String(id));
            return id;
        }

        if (
            typeof id === "string" &&
            id.trim() !== "" &&
            Number.isFinite(Number(id))
        ) {
            sessionStorage.setItem("userId", id.trim());
            return Number(id);
        }

    } catch (error) {
        console.error("Could not resolve signed-in user:", error);
    }

    return null;
}

/**
 * Returns initials for an avatar.
 */
function getInitials(name: string | null | undefined): string {

    if (!name || !name.trim()) return "?";

    return name.trim().slice(0, 2).toUpperCase();
}

/**
 * Returns a user's display name.
 */
function getDisplayName(user: UserSummary): string {

    return user.name || user.username;
}

export default function Friends() {


    const [currentUserId, setCurrentUserId] = useState<number | null>(null);

    const [authChecking, setAuthChecking] = useState(true);

    const [friends, setFriends] = useState<UserSummary[]>([]);

    const [incomingRequests, setIncomingRequests] = useState<RequestView[]>([]);

    const [outgoingRequests, setOutgoingRequests] = useState<RequestView[]>([]);

    const [blockedUsers, setBlockedUsers] = useState<UserSummary[]>([]);

    const [searchResults, setSearchResults] = useState<UserSearchResult[]>([]);

    const [query, setQuery] = useState("");

    const [dropdownOpen, setDropdownOpen] = useState(false);

    const [selectedFriend, setSelectedFriend] = useState<UserSummary | null>(null);

    const [actionMessage, setActionMessage] = useState("");

    const [loading, setLoading] = useState(false);

    const headerSearchRef = useRef<HTMLDivElement | null>(null);



    useEffect(() => {

        let active = true;

        resolveSignedInUserId().then((id) => {

            if (!active) return;

            setCurrentUserId(id);

            setAuthChecking(false);

        });

        return () => {
            active = false;
        };

    }, []);



    useEffect(() => {

        function handleClickOutside(e: MouseEvent) {

            if (
                headerSearchRef.current &&
                !headerSearchRef.current.contains(e.target as Node)
            ) {
                setDropdownOpen(false);
            }

        }

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };

    }, []);



    const loadAllSocialData = useCallback(async (userId: number) => {

        setLoading(true);

        const opts: RequestInit = {
            credentials: "include"
        };

        async function safeFetch<T>(
            url: string,
            fallback: T
        ): Promise<{ ok: boolean; data: T }> {

            try {

                const res = await fetch(url, opts);

                if (!res.ok) {
                    console.error(
                        `Request failed: ${url}`,
                        res.status,
                        await res.text()
                    );

                    return {
                        ok: false,
                        data: fallback
                    };
                }

                const json: unknown = await res.json();

                if (!Array.isArray(json)) {
                    console.error("Expected an array from:", url);

                    return {
                        ok: false,
                        data: fallback
                    };
                }

                return {
                    ok: true,
                    data: json as T
                };

            } catch (error) {

                console.error(`Error loading ${url}:`, error);

                return {
                    ok: false,
                    data: fallback
                };
            }
        }

        try {

            const [
                friendsRes,
                incomingRes,
                outgoingRes,
                blockedRes
            ] = await Promise.all([

                safeFetch<UserSummary[]>(
                    `${API_BASE}/user/${userId}`,
                    []
                ),

                safeFetch<RequestView[]>(
                    `${API_BASE}/requests/incoming/${userId}`,
                    []
                ),

                safeFetch<RequestView[]>(
                    `${API_BASE}/requests/outgoing/${userId}`,
                    []
                ),

                safeFetch<UserSummary[]>(
                    `${API_BASE}/blocked/${userId}`,
                    []
                )

            ]);

            if (friendsRes.ok) {

                setFriends(friendsRes.data);

                setSelectedFriend((prev) => {

                    if (!prev) {
                        return friendsRes.data[0] ?? null;
                    }

                    return (
                        friendsRes.data.find(
                            (friend) => friend.userId === prev.userId
                        ) ??
                        friendsRes.data[0] ??
                        null
                    );

                });

            }

            if (incomingRes.ok) {
                setIncomingRequests(incomingRes.data);
            }

            if (outgoingRes.ok) {
                setOutgoingRequests(outgoingRes.data);
            }

            if (blockedRes.ok) {
                setBlockedUsers(blockedRes.data);
            }

        } finally {
            setLoading(false);
        }

    }, []);

    // Load social data after authentication.

    useEffect(() => {

        if (currentUserId !== null) {
            loadAllSocialData(currentUserId);
        }

    }, [currentUserId, loadAllSocialData]);

    useEffect(() => {

        if (currentUserId === null) return;

        const trimmed = query.trim();

        if (!trimmed) {
            setSearchResults([]);
            return;
        }

        let cancelled = false;

        const timer = setTimeout(async () => {

            try {

                const res = await fetch(
                    `${API_BASE}/search?userId=${currentUserId}&query=${encodeURIComponent(trimmed)}`,
                    {
                        credentials: "include"
                    }
                );

                if (!res.ok) {

                    if (!cancelled) {
                        console.error("User search failed:", await res.text());
                        setSearchResults([]);
                    }

                    return;
                }

                const data: UserSearchResult[] = await res.json();

                if (!cancelled) {
                    setSearchResults(data);
                }

            } catch (error) {

                if (!cancelled) {

                    console.error("Search error:", error);

                    setSearchResults([]);
                }
            }

        }, 200);

        return () => {

            cancelled = true;

            clearTimeout(timer);

        };

    }, [currentUserId, query]);


    const suggestedUsernames = useMemo<UsernameOption[]>(() => {

        const q = query.trim().toLowerCase();

        if (!q) return [];

        const options = new Map<string, UsernameOption>();

        // Existing friends.

        for (const friend of friends) {

            const username = friend.username;

            const displayName = getDisplayName(friend);

            if (
                username.toLowerCase().includes(q) ||
                displayName.toLowerCase().includes(q)
            ) {

                options.set(username.toLowerCase(), {

                    id: friend.userId,

                    username,

                    label: displayName,

                    relationship: "FRIEND",

                    isFriend: true,

                    friend

                });

            }
        }

        // Search results from backend.

        for (const result of searchResults) {

            const user = result.user;

            const username = user.username;

            const displayName = getDisplayName(user);

            const relationship = result.relationship;

            if (!username) continue;

            const key = username.toLowerCase();

            if (!options.has(key)) {

                options.set(key, {

                    id: user.userId,

                    username,

                    label: displayName,

                    relationship,

                    isFriend: relationship === "FRIEND",

                    friend: undefined

                });

            }

        }

        return Array.from(options.values()).slice(0, 7);

    }, [query, friends, searchResults]);



    function handlePickSuggestion(option: UsernameOption) {

        setQuery(option.username);

        setDropdownOpen(false);

        if (option.isFriend && option.friend) {
            setSelectedFriend(option.friend);
        }

    }

    async function sendFriendRequestForUsername(rawUsername: string) {

        if (currentUserId === null) return;

        const username = rawUsername.trim();

        if (!username) return;

        try {

            const res = await fetch(`${API_BASE}/requests`, {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                credentials: "include",

                body: JSON.stringify({

                    requesterUserId: currentUserId,

                    targetUsername: username

                })

            });

            if (!res.ok) {

                const error = await res.json().catch(() => null);

                const message =
                    error?.message ?? "Could not send friend request.";

                setActionMessage(message);

                console.error("Friend request failed:", message);

                return;
            }

            setActionMessage("Friend request sent successfully.");

            setQuery("");

            setDropdownOpen(false);

            setSearchResults([]);

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error sending friend request:", error);

            setActionMessage("Could not connect to the server.");

        }

    }



    async function handleFormSubmit(e: React.FormEvent) {

        e.preventDefault();

        const trimmed = query.trim();

        if (!trimmed) return;

        const exactMatch = suggestedUsernames.find(
            (item) => item.username.toLowerCase() === trimmed.toLowerCase()
        );

        if (exactMatch?.isFriend && exactMatch.friend) {

            setSelectedFriend(exactMatch.friend);

            setDropdownOpen(false);

            return;
        }

        if (
            exactMatch &&
            exactMatch.relationship !== "NONE"
        ) {

            if (exactMatch.relationship === "OUTGOING_PENDING") {
                setActionMessage("You have already sent this user a friend request.");
            } else if (exactMatch.relationship === "INCOMING_PENDING") {
                setActionMessage("This user has already sent you a friend request.");
            }

            return;
        }

        await sendFriendRequestForUsername(
            exactMatch ? exactMatch.username : trimmed
        );

    }


    async function handleAcceptRequest(requesterUserId: number) {

        if (currentUserId === null) return;

        try {

            const res = await fetch(
                `${API_BASE}/requests/accept?userId=${currentUserId}&requesterUserId=${requesterUserId}`,
                {
                    method: "POST",
                    credentials: "include"
                }
            );

            if (!res.ok) {

                console.error(
                    "Accept request failed:",
                    await res.text()
                );

                return;
            }

            setActionMessage("Friend request accepted.");

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error accepting request:", error);

        }

    }


    async function handleDeclineRequest(requesterUserId: number) {

        if (currentUserId === null) return;

        try {

            const res = await fetch(
                `${API_BASE}/requests/decline?userId=${currentUserId}&requesterUserId=${requesterUserId}`,
                {
                    method: "POST",
                    credentials: "include"
                }
            );

            if (!res.ok) {

                console.error(
                    "Decline request failed:",
                    await res.text()
                );

                return;
            }

            setActionMessage("Friend request declined.");

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error declining request:", error);

        }

    }


    async function handleCancelRequest(recipientUserId: number) {

        if (currentUserId === null) return;

        try {

            const res = await fetch(
                `${API_BASE}/requests/cancel?userId=${currentUserId}&recipientUserId=${recipientUserId}`,
                {
                    method: "POST",
                    credentials: "include"
                }
            );

            if (!res.ok) {

                console.error(
                    "Cancel request failed:",
                    await res.text()
                );

                return;
            }

            setActionMessage("Friend request cancelled.");

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error cancelling request:", error);

        }

    }


    async function handleRemoveFriend(
        e: React.MouseEvent,
        friendUserId: number
    ) {

        e.stopPropagation();

        if (currentUserId === null) return;

        try {

            const res = await fetch(
                `${API_BASE}/${currentUserId}/${friendUserId}`,
                {
                    method: "DELETE",
                    credentials: "include"
                }
            );

            if (!res.ok) {

                console.error(
                    "Remove friend failed:",
                    await res.text()
                );

                return;
            }

            if (
                selectedFriend &&
                selectedFriend.userId === friendUserId
            ) {
                setSelectedFriend(null);
            }

            setActionMessage("Friend removed.");

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error removing friend:", error);

        }

    }



    async function handleBlockUser(targetUserId: number) {

        if (currentUserId === null) return;

        try {

            const res = await fetch(
                `${API_BASE}/block/${currentUserId}/${targetUserId}`,
                {
                    method: "POST",
                    credentials: "include"
                }
            );

            if (!res.ok) {

                console.error(
                    "Block user failed:",
                    await res.text()
                );

                return;
            }

            setSelectedFriend(null);

            setActionMessage("User blocked.");

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error blocking user:", error);

        }

    }




    async function handleUnblockUser(targetUserId: number) {

        if (currentUserId === null) return;

        try {

            const res = await fetch(
                `${API_BASE}/block/${currentUserId}/${targetUserId}`,
                {
                    method: "DELETE",
                    credentials: "include"
                }
            );

            if (!res.ok) {

                console.error(
                    "Unblock user failed:",
                    await res.text()
                );

                return;
            }

            setActionMessage("User unblocked.");

            await loadAllSocialData(currentUserId);

        } catch (error) {

            console.error("Error unblocking user:", error);

        }

    }



    if (authChecking) {

        return (
            <div className="friends">

                <div className="friends-header">

                    <p className="no-friends-msg">
                        Checking session...
                    </p>

                </div>

            </div>
        );

    }

    if (currentUserId === null) {

        return (
            <div className="friends">

                <div className="friends-header">

                    <div>

                        <h1>Friends</h1>

                        <p className="no-friends-msg">
                            Log in to view your friends.
                        </p>

                    </div>

                    <a href="/login" className="friends-input">
                        Log in
                    </a>

                </div>

            </div>
        );

    }




    const selectedId = selectedFriend?.userId ?? 0;

    const selectedUsername = selectedFriend?.username ?? "";

    const selectedDisplay = selectedFriend
        ? getDisplayName(selectedFriend)
        : "";



    return (

        <div className="friends">

            {/* HEADER */}

            <header className="friends-header">

                <div>

                    <h1>Friends</h1>

                </div>

                {/* SEARCH AND ADD */}

                <div
                    className="friends-search-box"
                    ref={headerSearchRef}
                >

                    <form
                        className="friends-add-form"
                        onSubmit={handleFormSubmit}
                    >

                        <input
                            type="text"
                            className="friends-input"
                            placeholder="Search or add username..."
                            value={query}
                            onFocus={() => {
                                if (query.trim()) {
                                    setDropdownOpen(true);
                                }
                            }}
                            onChange={(e) => {
                                setQuery(e.target.value);
                                setDropdownOpen(true);
                                setActionMessage("");
                            }}
                            onKeyDown={(e) => {
                                if (e.key === "Escape") {
                                    setDropdownOpen(false);
                                }
                            }}
                        />

                        <button
                            type="submit"
                            className="friends-input friends-action-btn"
                        >
                            Add Friend
                        </button>

                    </form>

                    {/* SEARCH DROPDOWN */}

                    {dropdownOpen && query.trim().length > 0 && (

                        <div className="friends-dropdown">

                            {suggestedUsernames.length === 0 ? (

                                <div className="friends-dropdown-empty">
                                    No usernames found
                                </div>

                            ) : (

                                suggestedUsernames.map((item) => (

                                    <div
                                        key={item.id}
                                        className="friends-dropdown-item"
                                        onMouseDown={(e) => {
                                            e.preventDefault();
                                            handlePickSuggestion(item);
                                        }}
                                    >

                                        <span className="friends-dropdown-handle">
                                            @{item.username}
                                        </span>

                                        {item.relationship === "NONE" && (

                                            <button
                                                type="button"
                                                className="friends-dropdown-add"
                                                onMouseDown={(e) => {
                                                    e.preventDefault();
                                                    e.stopPropagation();
                                                    sendFriendRequestForUsername(item.username);
                                                }}
                                            >
                                                Add
                                            </button>

                                        )}

                                        {item.relationship === "OUTGOING_PENDING" && (
                                            <span>Request sent</span>
                                        )}

                                        {item.relationship === "INCOMING_PENDING" && (
                                            <span>Request received</span>
                                        )}

                                        {item.relationship === "FRIEND" && (
                                            <span>Friend</span>
                                        )}

                                    </div>

                                ))

                            )}

                        </div>

                    )}

                </div>

            </header>

            {/* ACTION MESSAGE */}

            {actionMessage && (

                <p className="no-friends-msg" role="status">
                    {actionMessage}
                </p>

            )}

            {/* STATISTICS */}

            <div className="friends-stats">

                <div>
                    Friends: <strong>{friends.length}</strong>
                </div>

                <div>
                    Incoming: <strong>{incomingRequests.length}</strong>
                </div>

                <div>
                    Outgoing: <strong>{outgoingRequests.length}</strong>
                </div>

                <div>
                    Blocked: <strong>{blockedUsers.length}</strong>
                </div>

            </div>

            {loading && (
                <p className="no-friends-msg">
                    Loading social data...
                </p>
            )}

            {/* MAIN LAYOUT */}

            <div className="friends-layout">

                <div className="friends-main-content">

                    {/* CURRENT FRIENDS */}

                    <section className="friends-list-section">

                        <h2>Current Friends</h2>

                        {friends.length === 0 ? (

                            <p className="no-friends-msg">
                                No friends yet.
                            </p>

                        ) : (

                            <div className="friends-grid">

                                {friends.map((friend) => (

                                    <div
                                        key={friend.userId}
                                        className="friend-card"
                                        onClick={() => setSelectedFriend(friend)}
                                    >

                                        <div className="friends-card-avatar">
                                            {getInitials(getDisplayName(friend))}
                                        </div>

                                        <div className="friend-card-info">

                                            <h3>
                                                {getDisplayName(friend)}
                                            </h3>

                                            <p>
                                                @{friend.username}
                                            </p>

                                            <span className="friend-status">
                                                {friend.status || "Friend"}
                                            </span>

                                        </div>

                                        <div className="friend-card-actions">

                                            <button
                                                type="button"
                                                className="friend-delete-btn"
                                                onClick={(e) => handleRemoveFriend(
                                                    e,
                                                    friend.userId
                                                )}
                                            >
                                                Remove
                                            </button>

                                        </div>

                                    </div>

                                ))}

                            </div>

                        )}

                    </section>

                </div>

                {/* SIDEBAR */}

                <aside className="friends-sidebar">

                    {/* PROFILE PREVIEW */}

                    <section className="friends-profile-preview">

                        <h2>Profile Preview</h2>

                        {selectedFriend ? (

                            <div className="preview-details">

                                <div className="preview-avatar">
                                    {getInitials(selectedDisplay)}
                                </div>

                                <h3>
                                    {selectedDisplay}
                                </h3>

                                <p>
                                    @{selectedUsername}
                                </p>

                                {selectedFriend.location && (
                                    <p>
                                        Location: {selectedFriend.location}
                                    </p>
                                )}

                                <p>
                                    Games played: {selectedFriend.gamesPlayed}
                                </p>

                                <button
                                    type="button"
                                    className="friend-delete-btn"
                                    style={{ marginTop: "8px" }}
                                    onClick={() => handleBlockUser(selectedId)}
                                >
                                    Block
                                </button>

                            </div>

                        ) : (

                            <p className="preview-placeholder">
                                Select a friend to preview.
                            </p>

                        )}

                    </section>

                    {/* INCOMING REQUESTS */}

                    <section className="friends-requests">

                        <h2>Incoming Requests</h2>

                        {incomingRequests.length === 0 ? (

                            <p className="requests-placeholder">
                                No incoming requests.
                            </p>

                        ) : (

                            <div className="friends-grid">

                                {incomingRequests.map((req) => (

                                    <div
                                        key={req.otherUserId}
                                        className="friend-card"
                                    >

                                        <div className="friend-card-info">

                                            <h3>
                                                {req.otherUsername}
                                            </h3>

                                        </div>

                                        <div
                                            className="friend-card-actions"
                                            style={{ gap: "6px" }}
                                        >

                                            <button
                                                type="button"
                                                className="friends-input friends-action-btn"
                                                onClick={() => handleAcceptRequest(
                                                    req.otherUserId
                                                )}
                                            >
                                                Accept
                                            </button>

                                            <button
                                                type="button"
                                                className="friend-delete-btn"
                                                onClick={() => handleDeclineRequest(
                                                    req.otherUserId
                                                )}
                                            >
                                                Decline
                                            </button>

                                        </div>

                                    </div>

                                ))}

                            </div>

                        )}

                    </section>

                    {/* OUTGOING REQUESTS */}

                    <section className="friends-requests">

                        <h2>Outgoing Requests</h2>

                        {outgoingRequests.length === 0 ? (

                            <p className="requests-placeholder">
                                No outgoing requests.
                            </p>

                        ) : (

                            <div className="friends-grid">

                                {outgoingRequests.map((req) => (

                                    <div
                                        key={req.otherUserId}
                                        className="friend-card"
                                    >

                                        <div className="friend-card-info">

                                            <h3>
                                                {req.otherUsername}
                                            </h3>

                                        </div>

                                        <div className="friend-card-actions">

                                            <button
                                                type="button"
                                                className="friend-delete-btn"
                                                onClick={() => handleCancelRequest(
                                                    req.otherUserId
                                                )}
                                            >
                                                Cancel
                                            </button>

                                        </div>

                                    </div>

                                ))}

                            </div>

                        )}

                    </section>

                    {/* BLOCKED USERS */}

                    <section className="friends-requests">

                        <h2>Blocked</h2>

                        {blockedUsers.length === 0 ? (

                            <p className="requests-placeholder">
                                No blocked users.
                            </p>

                        ) : (

                            <div className="friends-grid">

                                {blockedUsers.map((user) => (

                                    <div
                                        key={user.userId}
                                        className="friend-card"
                                    >

                                        <div className="friend-card-info">

                                            <h3>
                                                @{user.username}
                                            </h3>

                                        </div>

                                        <div className="friend-card-actions">

                                            <button
                                                type="button"
                                                className="friends-input friends-action-btn"
                                                onClick={() => handleUnblockUser(
                                                    user.userId
                                                )}
                                            >
                                                Unblock
                                            </button>

                                        </div>

                                    </div>

                                ))}

                            </div>

                        )}

                    </section>

                </aside>

            </div>

        </div>

    );

}
