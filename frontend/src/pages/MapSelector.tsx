// Lets a player pick a map to start a quiz, with each map's leaderboard beside it.

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { startQuiz } from "../services/gameplayAPI";
import MapLeaderboard from "../components/MapLeaderboard";
import {
    getMaps,
    getMapImageUrl,
    type MapSummary,
} from "../api/maps";
import "./MapSelector.css";

function MapSelector() {
    const navigate = useNavigate();

    const [maps, setMaps] = useState<MapSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [startingMapId, setStartingMapId] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    // Fetch maps when the component loads.
    useEffect(() => {
        async function fetchMaps() {
            try {
                setLoading(true);
                setError(null);

                const fetchedMaps = await getMaps();
                setMaps(fetchedMaps);
            } catch (err) {
                console.error("Error fetching maps:", err);
                setError("Unable to load maps. Please try again.");
            } finally {
                setLoading(false);
            }
        }

        void fetchMaps();
    }, []);

    async function selectMap(mapId: number) {
        setStartingMapId(mapId);
        setError(null);

        const userId = sessionStorage.getItem("userId");

        try {
            const quiz = await startQuiz(
                String(mapId),
                userId === null ? null : Number(userId)
            );

            navigate("/quiz", {
                state: {
                    sessionId: quiz.sessionId,
                    mapId: quiz.mapId,
                    initialTarget: quiz.currentTarget,
                },
            });
        } catch (err) {
            console.error(err);
            setError("Unable to start the quiz.");
            setStartingMapId(null);
        }
    }

    if (loading) {
        return (
            <div className="map-selector">
                <h1>Choose a Map</h1>
                <p>Loading maps...</p>
            </div>
        );
    }

    return (
        <div className="map-selector">

            <div className="map-selector-header">
                <h1>Choose a Map</h1>
                <p>Select a map to start playing</p>

                {error && <p>{error}</p>}
            </div>

            {maps.length === 0 && !error && (
                <p>No maps are currently available.</p>
            )}

            <div className="map-grid">

                {maps.map((map) => {
                    const imageUrl = getMapImageUrl(map.imageRef);
                    const available = map.regionCount > 0;

                    return (
                        <div className="map-row" key={map.mapId}>

                            <div
                                className="map-card"
                                onClick={() => {
                                    if (
                                        available &&
                                        startingMapId === null
                                    ) {
                                        void selectMap(map.mapId);
                                    }
                                }}
                                style={{
                                    cursor: available
                                        ? "pointer"
                                        : "not-allowed",
                                    opacity: available ? 1 : 0.6,
                                }}
                            >

                                <div className="map-image-container">
                                    {imageUrl ? (
                                        <img
                                            src={imageUrl}
                                            alt={map.name}
                                        />
                                    ) : (
                                        <div className="map-no-image">
                                            No image available
                                        </div>
                                    )}
                                </div>

                                <div className="map-card-content">

                                    <h2>{map.name}</h2>

                                    <p>
                                        {map.regionCount} regions
                                    </p>

                                    <div className="map-card-footer">

                                        <span className="difficulty">
                                            {map.regionCount > 30
                                                ? "Hard"
                                                : map.regionCount > 10
                                                ? "Medium"
                                                : "Easy"}
                                        </span>

                                        <span>
                                            {!available
                                                ? "Coming soon"
                                                : startingMapId === map.mapId
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
        </div>
    );
}

export default MapSelector;