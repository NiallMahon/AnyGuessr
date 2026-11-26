import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import "./Friends.css";

interface UserSummary {
    id?: number;
    userId?: number;
    username?: string | null;
    name?: string | null;
    displayName?: string | null;
    status?: string | null;
    bio?: string | null;
    location?: string | null;
    initials?: string | null;
    gamesPlayed?: number | null;
}

interface RequestView {
    id?: number | string;
    requestId?: number | string;
    requesterUserId?: number | string;
    requesterId?: number | string;
    recipientUserId?: number | string;
    recipientId?: number | string;
    targetUserId?: number | string;
    otherUserId?: number | string;
    otherUsername?: string | null;
    requesterUsername?: string | null;
    recipientUsername?: string | null;
    targetUsername?: string | null;
    username?: string | null;
    displayName?: string | null;
    recipient?: Record<string, unknown> | null;
    targetUser?: Record<string, unknown> | null;
    user?: Record<string, unknown> | null;
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

/** Unwraps a nested `user` object if present, otherwise returns the record object. */
function unwrapUserRecord(raw: unknown): Record<string, unknown> | null {
    if (!raw || typeof raw !== "object") return null;
    const obj = raw as Record<string, unknown>;
    if (obj.user && typeof obj.user === "object") {
        return obj.user as Record<string, unknown>;
    }
    return obj;
}

/** Converts a number or numeric string into a finite number, or returns null. */
function toFiniteNumber(val: unknown): number | null {
    if (typeof val === "number" && Number.isFinite(val)) return val;
    if (typeof val === "string" && val.trim() !== "" && Number.isFinite(Number(val))) {
        return Number(val);
    }
    return null;
}

/** Extracts a user ID from a user or wrapped user record, with a fallback. */
function extractUserId(raw: unknown, fallback = 0): number {
    const userObj = unwrapUserRecord(raw);
    const candidate = userObj?.userId ?? userObj?.id ?? (raw as Record<string, unknown> | null)?.id;
    return toFiniteNumber(candidate) ?? fallback;
}

/** Extracts the best available username/handle string from a user record. */
function extractUsername(raw: unknown): string {
    const userObj = unwrapUserRecord(raw);
    if (!userObj) return "";
    const candidates = [userObj.username, userObj.userName, userObj.handle, userObj.name, userObj.displayName];
    for (const val of candidates) {
        if (typeof val === "string" && val.trim() !== "") {
            return val.trim();
        }
    }
    return "";
}

/** Extracts a display name for a user record, falling back to the provided default. */
function extractDisplayName(raw: unknown, fallback: string): string {
    const userObj = unwrapUserRecord(raw);
    if (!userObj) return fallback;
    const candidates = [userObj.displayName, userObj.name, userObj.username, userObj.userName];
    for (const val of candidates) {
        if (typeof val === "string" && val.trim() !== "") {
            return val.trim();
        }
    }
    return fallback;
}

/** Extracts the recipient/target user ID from an outgoing request object. */
function extractRecipientId(req: RequestView): number | null {
    const nestedRecipient = req.recipient ?? req.targetUser ?? req.user ?? null;
    const nestedId =
        nestedRecipient && typeof nestedRecipient === "object"
            ? toFiniteNumber(
                (nestedRecipient as Record<string, unknown>).userId ??
                (nestedRecipient as Record<string, unknown>).id
            )
            : null;

    return (
        toFiniteNumber(req.recipientUserId) ??
        toFiniteNumber(req.recipientId) ??
        toFiniteNumber(req.targetUserId) ??
        toFiniteNumber(req.otherUserId) ??
        nestedId ??
        toFiniteNumber(req.id) ??
        toFiniteNumber(req.requestId)
    );
}

/** Extracts a readable username/label for a friend request item. */
function extractRequestLabel(req: RequestView, fallbackId: number | string): string {
    const nestedRecipient = req.recipient ?? req.targetUser ?? req.user ?? null;
    const nestedName =
        nestedRecipient && typeof nestedRecipient === "object"
            ? ((nestedRecipient as Record<string, unknown>).username ??
                (nestedRecipient as Record<string, unknown>).name)
            : null;

    const candidate =
        req.otherUsername ??
        req.recipientUsername ??
        req.targetUsername ??
        req.username ??
        req.displayName ??
        req.requesterUsername ??
        nestedName;

    if (typeof candidate === "string" && candidate.trim() !== "") {
        return candidate.trim();
    }
    return `User #${fallbackId}`;
}

/** Resolves the signed-in user's ID from sessionStorage or the `/profile/me` endpoint. */
async function resolveSignedInUserId(): Promise<number | null> {
    const storedId = sessionStorage.getItem("userId");
    if (storedId && !Number.isNaN(Number(storedId)) && storedId.trim() !== "") {
        return Number(storedId);
    }

    try {
        const res = await fetch(PROFILE_ME_URL, { credentials: "include" });
        if (!res.ok) return null;
        const data = await res.json();
        const id = data?.id ?? data?.userId;
        if (typeof id === "number" && !Number.isNaN(id)) {
            sessionStorage.setItem("userId", String(id));
            return id;
        }
        if (typeof id === "string" && id.trim() !== "" && !Number.isNaN(Number(id))) {
            sessionStorage.setItem("userId", id.trim());
            return Number(id);
        }
    } catch {
        return null;
    }

    return null;
}

/** Returns up to two uppercase initials for an avatar badge. */
function getInitials(name: string | null | undefined): string {
    const trimmed = typeof name === "string" ? name.trim() : "";
    if (!trimmed) return "?";
    return trimmed.slice(0, 2).toUpperCase();
}

/** Renders the Friends page with search/add dropdown, friends list, preview, requests, and blocked users. */
export default function Friends() {
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [authChecking, setAuthChecking] = useState(true);

    const [friends, setFriends] = useState<UserSummary[]>([]);
    const [incomingRequests, setIncomingRequests] = useState<RequestView[]>([]);
    const [outgoingRequests, setOutgoingRequests] = useState<RequestView[]>([]);
    const [blockedUsers, setBlockedUsers] = useState<UserSummary[]>([]);
    const [searchResults, setSearchResults] = useState<unknown[]>([]);

    const [query, setQuery] = useState("");
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [selectedFriend, setSelectedFriend] = useState<UserSummary | null>(null);

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
            if (headerSearchRef.current && !headerSearchRef.current.contains(e.target as Node)) {
                setDropdownOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    /** Fetches friends, incoming/outgoing requests, and blocked users for the active user. */
    const loadAllSocialData = useCallback(async (userId: number) => {
        const opts: RequestInit = { credentials: "include" };

        async function safeFetch<T>(url: string, fallback: T): Promise<{ ok: boolean; data: T }> {
            try {
                const res = await fetch(url, opts);
                if (!res.ok) return { ok: false, data: fallback };
                const json = await res.json();
                return { ok: true, data: (Array.isArray(json) ? json : fallback) as T };
            } catch {
                return { ok: false, data: fallback };
            }
        }

        const [friendsRes, incomingRes, outgoingRes, blockedRes] = await Promise.all([
            safeFetch<UserSummary[]>(`${API_BASE}/user/${userId}`, []),
            safeFetch<RequestView[]>(`${API_BASE}/requests/incoming/${userId}`, []),
            safeFetch<RequestView[]>(`${API_BASE}/requests/outgoing/${userId}`, []),
            safeFetch<UserSummary[]>(`${API_BASE}/blocked/${userId}`, []),
        ]);

        if (friendsRes.ok) {
            setFriends(friendsRes.data);
            setSelectedFriend((prev) => {
                if (!prev) return friendsRes.data[0] ?? null;
                const prevId = extractUserId(prev, -1);
                return friendsRes.data.find((f) => extractUserId(f, -2) === prevId) ?? friendsRes.data[0] ?? null;
            });
        }
        if (incomingRes.ok) setIncomingRequests(incomingRes.data);
        if (outgoingRes.ok) setOutgoingRequests(outgoingRes.data);
        if (blockedRes.ok) setBlockedUsers(blockedRes.data);
    }, []);

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
                    { credentials: "include" }
                );
                if (!res.ok || cancelled) return;
                const data = await res.json();
                if (cancelled) return;
                if (Array.isArray(data)) {
                    setSearchResults(data);
                } else if (data && Array.isArray((data as { content?: unknown[] }).content)) {
                    setSearchResults((data as { content: unknown[] }).content);
                } else {
                    setSearchResults([]);
                }
            } catch {
                if (!cancelled) setSearchResults([]);
            }
        }, 150);

        return () => {
            cancelled = true;
            clearTimeout(timer);
        };
    }, [currentUserId, query]);

    const suggestedUsernames = useMemo<UsernameOption[]>(() => {
        const q = query.trim().toLowerCase();
        if (!q) return [];

        const options = new Map<string, UsernameOption>();

        for (const f of friends) {
            const uname = extractUsername(f);
            if (!uname) continue;
            const dname = extractDisplayName(f, uname);
            const fid = extractUserId(f, 0);

            if (uname.toLowerCase().includes(q) || dname.toLowerCase().includes(q)) {
                options.set(uname.toLowerCase(), {
                    id: fid,
                    username: uname,
                    label: dname,
                    relationship: "FRIEND",
                    isFriend: true,
                    friend: f,
                });
            }
        }

        for (const rawItem of searchResults) {
            const uname = extractUsername(rawItem);
            if (!uname) continue;
            const dname = extractDisplayName(rawItem, uname);
            const uid = extractUserId(rawItem, 0);
            const rel =
                rawItem &&
                typeof rawItem === "object" &&
                typeof (rawItem as { relationship?: unknown }).relationship === "string"
                    ? String((rawItem as { relationship: string }).relationship)
                    : "NONE";
            const key = uname.toLowerCase();

            if (!options.has(key)) {
                const matchedFriend = friends.find((f) => {
                    const funame = extractUsername(f);
                    return extractUserId(f, -1) === uid || (funame && funame.toLowerCase() === key);
                });
                const isFriend = Boolean(matchedFriend) || rel === "FRIEND";
                options.set(key, {
                    id: uid,
                    username: uname,
                    label: dname,
                    relationship: isFriend ? "FRIEND" : rel,
                    isFriend,
                    friend: matchedFriend,
                });
            }
        }

        return Array.from(options.values()).slice(0, 7);
    }, [query, friends, searchResults]);

    /** Selects a dropdown suggestion into the search input and previews if already a friend. */
    function handlePickSuggestion(option: UsernameOption) {
        setQuery(option.username);
        setDropdownOpen(false);
        if (option.isFriend && option.friend) {
            setSelectedFriend(option.friend);
        }
    }

    /** Sends a friend request to the given username and refreshes social lists. */
    async function sendFriendRequestForUsername(rawUsername: string) {
        if (!currentUserId) return;
        const username = rawUsername.trim();
        if (!username) return;

        const res = await fetch(`${API_BASE}/requests`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                requesterUserId: currentUserId,
                targetUsername: username,
            }),
        });
        if (res.ok) {
            setDropdownOpen(false);
            await loadAllSocialData(currentUserId);
        }
    }

    /** Submits the search/add form to preview an existing friend or send a friend request. */
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

        await sendFriendRequestForUsername(exactMatch ? exactMatch.username : trimmed);
    }

    /** Accepts an incoming friend request from `requesterUserId`. */
    async function handleAcceptRequest(requesterUserId: number) {
        if (!currentUserId) return;
        const res = await fetch(
            `${API_BASE}/requests/accept?userId=${currentUserId}&requesterUserId=${requesterUserId}`,
            { method: "POST", credentials: "include" }
        );
        if (res.ok) await loadAllSocialData(currentUserId);
    }

    /** Declines an incoming friend request from `requesterUserId`. */
    async function handleDeclineRequest(requesterUserId: number) {
        if (!currentUserId) return;
        const res = await fetch(
            `${API_BASE}/requests/decline?userId=${currentUserId}&requesterUserId=${requesterUserId}`,
            { method: "POST", credentials: "include" }
        );
        if (res.ok) await loadAllSocialData(currentUserId);
    }

    /** Cancels a pending outgoing friend request using supported endpoint variants. */
    async function handleCancelRequest(req: RequestView) {
        if (!currentUserId) return;
        const recipientId = extractRecipientId(req);
        const requestId = toFiniteNumber(req.id ?? req.requestId);

        const attempts: Array<{ url: string; method: "POST" | "DELETE" }> = [];
        if (recipientId !== null) {
            attempts.push({
                url: `${API_BASE}/requests/cancel?userId=${currentUserId}&recipientUserId=${recipientId}`,
                method: "POST",
            });
            attempts.push({
                url: `${API_BASE}/requests/cancel?userId=${currentUserId}&recipientUserId=${recipientId}`,
                method: "DELETE",
            });
            attempts.push({
                url: `${API_BASE}/requests/${currentUserId}/${recipientId}`,
                method: "DELETE",
            });
        }
        if (requestId !== null && requestId !== recipientId) {
            attempts.push({
                url: `${API_BASE}/requests/${requestId}`,
                method: "DELETE",
            });
        }

        for (const attempt of attempts) {
            try {
                const res = await fetch(attempt.url, {
                    method: attempt.method,
                    credentials: "include",
                });
                if (res.ok) {
                    await loadAllSocialData(currentUserId);
                    return;
                }
            } catch {
                // Try next candidate endpoint shape
            }
        }
    }

    /** Removes a friend connection for `friendUserId`. */
    async function handleRemoveFriend(e: React.MouseEvent, friendUserId: number) {
        e.stopPropagation();
        if (!currentUserId) return;
        const res = await fetch(`${API_BASE}/${currentUserId}/${friendUserId}`, {
            method: "DELETE",
            credentials: "include",
        });
        if (res.ok) {
            if (selectedFriend && extractUserId(selectedFriend, -1) === friendUserId) {
                setSelectedFriend(null);
            }
            await loadAllSocialData(currentUserId);
        }
    }

    /** Blocks `targetUserId` and refreshes social lists. */
    async function handleBlockUser(targetUserId: number) {
        if (!currentUserId) return;
        const res = await fetch(`${API_BASE}/block/${currentUserId}/${targetUserId}`, {
            method: "POST",
            credentials: "include",
        });
        if (res.ok) {
            if (selectedFriend && extractUserId(selectedFriend, -1) === targetUserId) {
                setSelectedFriend(null);
            }
            await loadAllSocialData(currentUserId);
        }
    }

    /** Unblocks `targetUserId` and refreshes social lists. */
    async function handleUnblockUser(targetUserId: number) {
        if (!currentUserId) return;
        const res = await fetch(`${API_BASE}/block/${currentUserId}/${targetUserId}`, {
            method: "DELETE",
            credentials: "include",
        });
        if (res.ok) await loadAllSocialData(currentUserId);
    }

    if (authChecking) {
        return (
            <div className="friends">
                <div className="friends-header">
                    <p className="no-friends-msg">Checking session...</p>
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
                        <p className="no-friends-msg">Log in to view your friends.</p>
                    </div>
                    <a href="/login" className="friends-input">
                        Log in
                    </a>
                </div>
            </div>
        );
    }

    const selectedId = selectedFriend ? extractUserId(selectedFriend, 0) : 0;
    const selectedUname = selectedFriend ? extractUsername(selectedFriend) : "";
    const selectedDisplay = selectedFriend
        ? extractDisplayName(selectedFriend, selectedUname || "Player")
        : "";
    const selectedBio =
        selectedFriend && typeof unwrapUserRecord(selectedFriend)?.bio === "string"
            ? String(unwrapUserRecord(selectedFriend)?.bio)
            : "";

    return (
        <div className="friends">
            <header className="friends-header">
                <div>
                    <h1>Friends</h1>
                </div>

                <div className="friends-search-box" ref={headerSearchRef}>
                    <form className="friends-add-form" onSubmit={handleFormSubmit}>
                        <input
                            type="text"
                            className="friends-input"
                            placeholder="Search or add username..."
                            value={query}
                            onFocus={() => {
                                if (query.trim()) setDropdownOpen(true);
                            }}
                            onChange={(e) => {
                                setQuery(e.target.value);
                                setDropdownOpen(true);
                            }}
                            onKeyDown={(e) => {
                                if (e.key === "Escape") setDropdownOpen(false);
                            }}
                        />
                        <button type="submit" className="friends-input friends-action-btn">
                            Add Friend
                        </button>
                    </form>

                    {dropdownOpen && query.trim().length > 0 && (
                        <div className="friends-dropdown">
                            {suggestedUsernames.length === 0 ? (
                                <div className="friends-dropdown-empty">No usernames found</div>
                            ) : (
                                suggestedUsernames.map((item) => (
                                    <div
                                        key={`${item.username}-${item.id}`}
                                        className="friends-dropdown-item"
                                        onMouseDown={(e) => {
                                            e.preventDefault();
                                            handlePickSuggestion(item);
                                        }}
                                    >
                                        <span className="friends-dropdown-handle">@{item.username}</span>
                                        {!item.isFriend && (
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
                                    </div>
                                ))
                            )}
                        </div>
                    )}
                </div>
            </header>

            <div className="friends-stats">
                <div>Friends: <strong>{friends.length}</strong></div>
                <div>Incoming: <strong>{incomingRequests.length}</strong></div>
                <div>Outgoing: <strong>{outgoingRequests.length}</strong></div>
                <div>Blocked: <strong>{blockedUsers.length}</strong></div>
            </div>

            <div className="friends-layout">
                <div className="friends-main-content">
                    <section className="friends-list-section">
                        <h2>Current Friends</h2>

                        {friends.length === 0 ? (
                            <p className="no-friends-msg">No friends yet.</p>
                        ) : (
                            <div className="friends-grid">
                                {friends.map((friend, idx) => {
                                    const fid = extractUserId(friend, idx);
                                    const uname = extractUsername(friend);
                                    const display = extractDisplayName(friend, uname || `User #${fid}`);
                                    const statusText =
                                        typeof unwrapUserRecord(friend)?.status === "string"
                                            ? String(unwrapUserRecord(friend)?.status)
                                            : "Friend";
                                    return (
                                        <div
                                            key={`${fid}-${idx}`}
                                            className="friend-card"
                                            onClick={() => setSelectedFriend(friend)}
                                        >
                                            <div className="friends-card-avatar">{getInitials(display)}</div>
                                            <div className="friend-card-info">
                                                <h3>{display}</h3>
                                                {uname && <p>@{uname}</p>}
                                                <span className="friend-status">{statusText}</span>
                                            </div>
                                            <div className="friend-card-actions">
                                                <button
                                                    type="button"
                                                    className="friend-delete-btn"
                                                    onClick={(e) => handleRemoveFriend(e, fid)}
                                                >
                                                    Remove
                                                </button>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </section>
                </div>

                <aside className="friends-sidebar">
                    <section className="friends-profile-preview">
                        <h2>Profile Preview</h2>
                        {selectedFriend ? (
                            <div className="preview-details">
                                <div className="preview-avatar">{getInitials(selectedDisplay)}</div>
                                <h3>{selectedDisplay}</h3>
                                {selectedUname && <p>@{selectedUname}</p>}
                                {selectedBio && <p>{selectedBio}</p>}
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
                            <p className="preview-placeholder">Select a friend to preview.</p>
                        )}
                    </section>

                    <section className="friends-requests">
                        <h2>Incoming Requests</h2>
                        {incomingRequests.length === 0 ? (
                            <p className="requests-placeholder">No incoming requests.</p>
                        ) : (
                            <div className="friends-grid">
                                {incomingRequests.map((req, i) => {
                                    const id = toFiniteNumber(req.requesterUserId ?? req.requesterId ?? req.otherUserId ?? req.id);
                                    const name = extractRequestLabel(req, id ?? i);
                                    return (
                                        <div key={`${id}-${i}`} className="friend-card">
                                            <div className="friend-card-info">
                                                <h3>{name}</h3>
                                            </div>
                                            <div className="friend-card-actions" style={{ gap: "6px" }}>
                                                <button
                                                    type="button"
                                                    className="friends-input friends-action-btn"
                                                    disabled={id === null}
                                                    onClick={() => id !== null && handleAcceptRequest(id)}
                                                >
                                                    Accept
                                                </button>
                                                <button
                                                    type="button"
                                                    className="friend-delete-btn"
                                                    disabled={id === null}
                                                    onClick={() => id !== null && handleDeclineRequest(id)}
                                                >
                                                    Decline
                                                </button>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </section>

                    <section className="friends-requests">
                        <h2>Outgoing Requests</h2>
                        {outgoingRequests.length === 0 ? (
                            <p className="requests-placeholder">No outgoing requests.</p>
                        ) : (
                            <div className="friends-grid">
                                {outgoingRequests.map((req, i) => {
                                    const recipientId = extractRecipientId(req);
                                    const fallbackKey = recipientId ?? req.id ?? i;
                                    const name = extractRequestLabel(req, fallbackKey);
                                    return (
                                        <div key={`${fallbackKey}-${i}`} className="friend-card">
                                            <div className="friend-card-info">
                                                <h3>{name}</h3>
                                            </div>
                                            <div className="friend-card-actions">
                                                <button
                                                    type="button"
                                                    className="friend-delete-btn"
                                                    onClick={() => handleCancelRequest(req)}
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </section>

                    <section className="friends-requests">
                        <h2>Blocked</h2>
                        {blockedUsers.length === 0 ? (
                            <p className="requests-placeholder">No blocked users.</p>
                        ) : (
                            <div className="friends-grid">
                                {blockedUsers.map((u, i) => {
                                    const uid = extractUserId(u, i);
                                    const uname = extractUsername(u) || `User #${uid}`;
                                    return (
                                        <div key={`${uid}-${i}`} className="friend-card">
                                            <div className="friend-card-info">
                                                <h3>{uname}</h3>
                                            </div>
                                            <div className="friend-card-actions">
                                                <button
                                                    type="button"
                                                    className="friends-input friends-action-btn"
                                                    onClick={() => handleUnblockUser(uid)}
                                                >
                                                    Unblock
                                                </button>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </section>
                </aside>
            </div>
        </div>
    );
}
