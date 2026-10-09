
import { useState } from "react";
import "./MapMaker.css";

type Point = {
    x: number;
    y: number;
};

function MapMaker() {
    const [background, setBackground] = useState<string | null>(null);
    const [currentPolygon, setCurrentPolygon] = useState<Point[]>([]);
    const [polygons, setPolygons] = useState<Point[][]>([]);
    const [confirmingPolygon, setConfirmingPolygon] = useState(false);

    const handleImageUpload = (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        const file = event.target.files?.[0];

        if (!file) return;

        const imageUrl = URL.createObjectURL(file);
        setBackground(imageUrl);
    };

    const handleCanvasClick = (
        event: React.MouseEvent<SVGSVGElement>
    ) => {
        if (confirmingPolygon) return;

        const svg = event.currentTarget;
        const rect = svg.getBoundingClientRect();

        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;

        setCurrentPolygon((previous) => [
            ...previous,
            { x, y }
        ]);
    };

    const finishPolygon = () => {
        if (currentPolygon.length < 3) {
            alert("Polygon needs at least 3 points.");
            return;
        }

        setConfirmingPolygon(true);
    };

    const confirmPolygon = () => {
        setPolygons((previous) => [
            ...previous,
            currentPolygon
        ]);

        setCurrentPolygon([]);
        setConfirmingPolygon(false);
    };

    const editPolygon = () => {
        setConfirmingPolygon(false);
    };

    const clearMap = () => {
        setCurrentPolygon([]);
        setPolygons([]);
        setBackground(null);
        setConfirmingPolygon(false);
    };

    return (
        <div className="map-maker">

            <h1>Create Your Map</h1>

            <div className="map-maker-controls">

                <label>
                    Upload background image:

                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageUpload}
                    />
                </label>

                <button
                    onClick={finishPolygon}
                    disabled={
                        currentPolygon.length < 3 ||
                        confirmingPolygon
                    }
                >
                    Finish Polygon
                </button>

                <button onClick={clearMap}>
                    Clear
                </button>

            </div>

            <div className="map-editor">

                {background && (
                    <img
                        src={background}
                        className="map-background"
                        alt="Map background"
                    />
                )}

                <svg
                    className="map-canvas"
                    width="800"
                    height="500"
                    onClick={handleCanvasClick}
                >

                    {/* Finished polygons */}
                    {polygons.map((polygon, index) => (
                        <polygon
                            key={index}
                            points={polygon
                                .map(
                                    (point) =>
                                        `${point.x},${point.y}`
                                )
                                .join(" ")
                            }
                            fill="rgba(0, 150, 255, 0.3)"
                            stroke="blue"
                            strokeWidth="2"
                        />
                    ))}

                    {/* Current polygon */}
                    {currentPolygon.length > 0 && (
                        <polygon
                            points={currentPolygon
                                .map(
                                    (point) =>
                                        `${point.x},${point.y}`
                                )
                                .join(" ")
                            }
                            fill={
                                confirmingPolygon
                                    ? "rgba(0, 255, 0, 0.3)"
                                    : "rgba(255, 0, 0, 0.15)"
                            }
                            stroke={
                                confirmingPolygon
                                    ? "green"
                                    : "red"
                            }
                            strokeWidth="2"
                        />
                    )}

                    {/* Points */}
                    {currentPolygon.map((point, index) => (
                        <circle
                            key={index}
                            cx={point.x}
                            cy={point.y}
                            r="5"
                            fill="red"
                        />
                    ))}

                </svg>

                {/* Confirmation box */}
                {confirmingPolygon && (
                    <div className="polygon-confirmation">

                        <p>
                            Are you happy with this polygon?
                        </p>

                        <button onClick={confirmPolygon}>
                            Yes, save polygon
                        </button>

                        <button onClick={editPolygon}>
                            No, edit polygon
                        </button>

                    </div>
                )}

            </div>

        </div>
    );
}

export default MapMaker;
