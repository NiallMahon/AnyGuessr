
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { startQuiz } from "../services/gameplayAPI";
import MapLeaderboard from "../components/MapLeaderboard";
import {
    getMaps,
    getMapDetails,
    getMapImageUrl,
    type MapSummary,
} from "../api/maps";

import "./MapSelector.css";

function MapPreview({
    imageUrl,
    mapName,
}: {
    imageUrl: string | null;
    mapName: string;
}) {
    const [imageFailed, setImageFailed] = useState(false);

    if (!imageUrl || imageFailed) {
        return (
            <div className="map-no-image">
                Map preview unavailable
            </div>
        );
    }

    return (
        <img
            src={imageUrl}
            alt={`Preview of ${mapName}`}
            onError={() => setImageFailed(true)}
        />
    );
}

function hasPlayableRegions(
    details: Awaited<ReturnType<typeof getMapDetails>>
): boolean {
    const validDimensions =
        Number.isFinite(details.width) &&
        Number.isFinite(details.height) &&
        details.width > 0 &&
        details.height > 0;

    const validRegions =
        Array.isArray(details.regions) &&
        details.regions.length > 0 &&
        details.regions.every(
            (region) =>
                Array.isArray(region.points) &&
                region.points.length >= 3 &&
                region.points.every(
                    (point) =>
                        Number.isFinite(point.x) &&
                        Number.isFinite(point.y)
                )
        );

    return validDimensions && validRegions;
}

function MapSelector() {
    const navigate = useNavigate();

    const [maps, setMaps] = useState<MapSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [startingMapId, setStartingMapId] = useState<number | null>(null);
    const [unavailableMapIds, setUnavailableMapIds] = useState<number[]>([]);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let active = true;

        async function fetchMaps() {
            try {
                const fetchedMaps = await getMaps();

                // Older polygon formats can make the map-details endpoint fail.
                const checkedMaps = await Promise.all(
                    fetchedMaps.map(async (map) => {
                        if (map.regionCount <= 0) {
                            return map.mapId;
                        }

                        try {
                            const details = await getMapDetails(map.mapId);

                            return hasPlayableRegions(details)
                                ? null
                                : map.mapId;
                        } catch (err) {
                            console.warn(
                                `Map ${map.mapId} is unavailable:`,
                                err
                            );

                            return map.mapId;
                        }
                    })
                );

                if (active) {
                    setMaps(fetchedMaps);
                    setUnavailableMapIds(
                        checkedMaps.filter(
                            (id): id is number => id !== null
                        )
                    );
                }
            } catch (err) {
                console.error("Error fetching maps:", err);

                if (active) {
                    setError("Unable to load maps. Please try again.");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        }

        void fetchMaps();

        return () => {
            active = false;
        };
    }, []);

    async function selectMap(mapId: number) {
        if (startingMapId !== null || unavailableMapIds.includes(mapId)) {
            return;
        }

        setStartingMapId(mapId);
        setError(null);

        try {
            const details = await getMapDetails(mapId);

            if (!hasPlayableRegions(details)) {
                throw new Error("Map has invalid polygon data.");
            }

            const storedUserId = sessionStorage.getItem("userId");
            const userId =
                storedUserId === null ? null : Number(storedUserId);

            const quiz = await startQuiz(String(mapId), userId);

            navigate("/quiz", {
                state: {
                    sessionId: quiz.sessionId,
                    mapId: quiz.mapId,
                    initialTarget: quiz.currentTarget,
                    targets: quiz.targets,
                },
            });
        } catch (err) {
            console.error("Unable to start selected map:", err);
            setError(
                "This map is currently unavailable. Please choose another map."
            );
            setStartingMapId(null);
        }
    }

    if (loading) {
        return (
            <main className="map-selector">
                <header className="map-selector-header">
                    <h1>Choose a Map</h1>
                    <p role="status">Checking available maps...</p>
                </header>
            </main>
        );
    }

    return (
        <main className="map-selector">
            <header className="map-selector-header">
                <h1>Choose a Map</h1>
                <p>Select a map to start playing</p>

                {error && (
                    <p
                        role="alert"
                        style={{ color: "#f87171", marginTop: "14px" }}
                    >
                        {error}
                    </p>
                )}
            </header>

            {maps.length === 0 && !error && (
                <p>No maps are currently available.</p>
            )}

            <div className="map-grid">
                {maps.map((map) => {
                    const imageUrl = getMapImageUrl(map.imageRef);
                    const available =
                        map.regionCount > 0 &&
                        !unavailableMapIds.includes(map.mapId);
                    const isStarting = startingMapId === map.mapId;
                    const disabled = !available || startingMapId !== null;
                    const difficulty =
                        map.regionCount > 30
                            ? "Hard"
                            : map.regionCount > 10
                              ? "Medium"
                              : "Easy";

                    return (
                        <div className="map-row" key={map.mapId}>
                            <div
                                className="map-card"
                                role="button"
                                tabIndex={disabled ? -1 : 0}
                                aria-disabled={disabled}
                                aria-label={
                                    available
                                        ? `Play ${map.name}`
                                        : `${map.name} unavailable`
                                }
                                onClick={() => {
                                    if (!disabled) {
                                        void selectMap(map.mapId);
                                    }
                                }}
                                onKeyDown={(event) => {
                                    if (
                                        !disabled &&
                                        (event.key === "Enter" ||
                                            event.key === " ")
                                    ) {
                                        event.preventDefault();
                                        void selectMap(map.mapId);
                                    }
                                }}
                                style={{
                                    cursor: disabled ? "not-allowed" : "pointer",
                                    opacity: available ? 1 : 0.6,
                                }}
                            >
                                <div className="map-image-container">
                                    <MapPreview
                                        key={imageUrl ?? "no-image"}
                                        imageUrl={imageUrl}
                                        mapName={map.name}
                                    />
                                </div>

                                <div className="map-card-content">
                                    <h2>{map.name}</h2>
                                    <p>{map.regionCount} regions</p>

                                    <div className="map-card-footer">
                                        <span className="difficulty">
                                            {difficulty}
                                        </span>
                                        <span>
                                            {!available
                                                ? "Unavailable"
                                                : isStarting
                                                  ? "Starting..."
                                                  : "Play"}
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <MapLeaderboard
                                mapId={map.mapId}
                                mapName={map.name}
                                available={available}
                            />
                        </div>
                    );
                })}
            </div>
        </main>
    );
}

export default MapSelector;