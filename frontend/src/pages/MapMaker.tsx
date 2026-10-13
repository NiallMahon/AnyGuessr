import { useRef, useState } from "react";
import type {
    ChangeEvent,
    MouseEvent as ReactMouseEvent,
    PointerEvent as ReactPointerEvent,
} from "react";
import "./MapMaker.css";
import { exportMap, uploadMapImage } from "../api/maps";
import type { MapExportPayload, Point } from "../api/maps";

type Polygon = {
    id: number;
    name: string;
    points: Point[];
};

type Mode = "draw" | "edit";
type DragTarget = "draft" | number;
type Status = { kind: "error" | "success"; text: string };

const DEFAULT_WIDTH = 800;
const DEFAULT_HEIGHT = 500;
const COLORS = [
    "#3b82f6",
    "#ef4444",
    "#10b981",
    "#f59e0b",
    "#8b5cf6",
    "#ec4899",
    "#14b8a6",
    "#f97316",
];

const clamp = (value: number, min: number, max: number) =>
    Math.min(Math.max(value, min), max);

const toPointsAttr = (points: Point[]) =>
    points.map((p) => `${p.x},${p.y}`).join(" ");

const centroid = (points: Point[]): Point => ({
    x: points.reduce((sum, p) => sum + p.x, 0) / points.length,
    y: points.reduce((sum, p) => sum + p.y, 0) / points.length,
});

function MapMaker() {
    const svgRef = useRef<SVGSVGElement>(null);
    const dragRef = useRef<{ target: DragTarget; index: number } | null>(null);
    const nextId = useRef(1);

    const [mapId, setMapId] = useState<number | null>(null);
    const [mapName, setMapName] = useState("");

    const [imageUrl, setImageUrl] = useState<string | null>(null);
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imageRef, setImageRef] = useState<string | null>(null);
    const [size, setSize] = useState({
        width: DEFAULT_WIDTH,
        height: DEFAULT_HEIGHT,
    });

    const [polygons, setPolygons] = useState<Polygon[]>([]);
    const [draft, setDraft] = useState<Point[]>([]);
    const [draftName, setDraftName] = useState("");
    const [confirming, setConfirming] = useState(false);
    const [confirmError, setConfirmError] = useState("");

    const [mode, setMode] = useState<Mode>("draw");
    const [selectedId, setSelectedId] = useState<number | null>(null);

    const [status, setStatus] = useState<Status | null>(null);
    const [exporting, setExporting] = useState(false);

    /* ------------------------------------------------------------ helpers */

    // Converts a mouse/touch position into the SVG's own coordinate space,
    // so points stay correct however large the canvas is displayed.
    const toSvgPoint = (event: { clientX: number; clientY: number }): Point => {
        const svg = svgRef.current;
        const matrix = svg?.getScreenCTM();
        if (!svg || !matrix) return { x: 0, y: 0 };

        const p = new DOMPoint(event.clientX, event.clientY).matrixTransform(
            matrix.inverse()
        );
        return {
            x: clamp(p.x, 0, size.width),
            y: clamp(p.y, 0, size.height),
        };
    };

    // Resizes the coordinate space and rescales existing points so they
    // stay in the same place relative to the image.
    const resizeCanvas = (width: number, height: number) => {
        const scaleX = width / size.width;
        const scaleY = height / size.height;
        const scale = (points: Point[]) =>
            points.map((p) => ({ x: p.x * scaleX, y: p.y * scaleY }));

        setPolygons((prev) =>
            prev.map((poly) => ({ ...poly, points: scale(poly.points) }))
        );
        setDraft((prev) => scale(prev));
        setSize({ width, height });
    };

    /* --------------------------------------------------------- background */

    const handleImageUpload = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = ""; // allow re-selecting the same file later
        if (!file) return;

        const url = URL.createObjectURL(file);
        const img = new Image();

        img.onload = () => {
            if (imageUrl) URL.revokeObjectURL(imageUrl);

            // Fit the canvas to the image's aspect ratio so polygon
            // coordinates map directly onto the image.
            const width = DEFAULT_WIDTH;
            const height = Math.round(
                (DEFAULT_WIDTH * img.naturalHeight) / img.naturalWidth
            );
            resizeCanvas(width, height);

            setImageUrl(url);
            setImageFile(file);
            setImageRef(null); // new image needs uploading on export
        };
        img.onerror = () => {
            URL.revokeObjectURL(url);
            setStatus({ kind: "error", text: "Could not read that image." });
        };
        img.src = url;
    };

    const removeBackground = () => {
        if (imageUrl) URL.revokeObjectURL(imageUrl);
        setImageUrl(null);
        setImageFile(null);
        setImageRef(null);
        resizeCanvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    };

    /* --------------------------------------------------- point dragging */

    const startDrag = (
        event: ReactPointerEvent<SVGCircleElement>,
        target: DragTarget,
        index: number
    ) => {
        event.stopPropagation();
        event.currentTarget.setPointerCapture(event.pointerId);
        dragRef.current = { target, index };
    };

    const onDrag = (event: ReactPointerEvent<SVGCircleElement>) => {
        const drag = dragRef.current;
        if (!drag) return;

        const p = toSvgPoint(event);
        const move = (points: Point[]) =>
            points.map((pt, i) => (i === drag.index ? p : pt));

        if (drag.target === "draft") {
            setDraft(move);
        } else {
            setPolygons((prev) =>
                prev.map((poly) =>
                    poly.id === drag.target
                        ? { ...poly, points: move(poly.points) }
                        : poly
                )
            );
        }
    };

    const endDrag = (event: ReactPointerEvent<SVGCircleElement>) => {
        dragRef.current = null;
        if (event.currentTarget.hasPointerCapture(event.pointerId)) {
            event.currentTarget.releasePointerCapture(event.pointerId);
        }
    };

    // Double-click a point to delete it (a polygon keeps at least 3 points).
    const removePoint = (target: DragTarget, index: number) => {
        if (target === "draft") {
            setDraft((prev) => prev.filter((_, i) => i !== index));
            setConfirming(false);
            return;
        }
        setPolygons((prev) =>
            prev.map((poly) =>
                poly.id === target && poly.points.length > 3
                    ? { ...poly, points: poly.points.filter((_, i) => i !== index) }
                    : poly
            )
        );
    };

    /* ------------------------------------------------------ drawing flow */

    const handleCanvasClick = (event: ReactMouseEvent<SVGSVGElement>) => {
        if (mode === "edit") {
            setSelectedId(null);
            return;
        }
        if (confirming) return;

        const p = toSvgPoint(event);
        setSelectedId(null);
        setDraft((prev) => [...prev, p]);
    };

    const startDrawing = () => {
        setMode("draw");
        setSelectedId(null);
    };

    const undoPoint = () => {
        setDraft((prev) => prev.slice(0, -1));
        setConfirming(false);
    };

    const cancelDraft = () => {
        setDraft([]);
        setDraftName("");
        setConfirming(false);
        setConfirmError("");
    };

    const finishPolygon = () => {
        if (draft.length < 3) return;
        if (!draftName.trim()) setDraftName(`Region ${polygons.length + 1}`);
        setConfirmError("");
        setConfirming(true);
    };

    const confirmPolygon = () => {
        const name = draftName.trim();

        if (!name) {
            setConfirmError("Give the polygon a name.");
            return;
        }
        if (polygons.some((p) => p.name.toLowerCase() === name.toLowerCase())) {
            setConfirmError("Another polygon already uses that name.");
            return;
        }

        const id = nextId.current++;
        setPolygons((prev) => [...prev, { id, name, points: draft }]);
        setDraft([]);
        setDraftName("");
        setConfirming(false);
        setConfirmError("");
        setMode("edit");
        setSelectedId(id);
    };

    /* ----------------------------------------------------- polygon list */

    const renamePolygon = (id: number, name: string) => {
        setPolygons((prev) =>
            prev.map((poly) => (poly.id === id ? { ...poly, name } : poly))
        );
    };

    const deletePolygon = (id: number) => {
        setPolygons((prev) => prev.filter((poly) => poly.id !== id));
        setSelectedId((prev) => (prev === id ? null : prev));
    };

    const selectPolygon = (id: number) => {
        if (draft.length > 0) return; // finish or cancel the draft first
        setMode("edit");
        setSelectedId(id);
    };

    const clearAll = () => {
        if (
            (polygons.length > 0 || draft.length > 0) &&
            !window.confirm("Clear everything and start a new map?")
        ) {
            return;
        }
        if (imageUrl) URL.revokeObjectURL(imageUrl);
        setMapId(null);
        setMapName("");
        setImageUrl(null);
        setImageFile(null);
        setImageRef(null);
        setSize({ width: DEFAULT_WIDTH, height: DEFAULT_HEIGHT });
        setPolygons([]);
        setDraft([]);
        setDraftName("");
        setConfirming(false);
        setConfirmError("");
        setSelectedId(null);
        setMode("draw");
        setStatus(null);
    };

    /* ------------------------------------------------------------ export */

    const handleExport = async () => {
        setStatus(null);

        const name = mapName.trim();
        if (!name) {
            setStatus({ kind: "error", text: "Give the map a name." });
            return;
        }
        if (draft.length > 0) {
            setStatus({
                kind: "error",
                text: "Confirm or cancel the polygon you're drawing first.",
            });
            return;
        }
        if (polygons.length === 0) {
            setStatus({ kind: "error", text: "Add at least one polygon." });
            return;
        }

        const names = polygons.map((p) => p.name.trim().toLowerCase());
        if (names.some((n) => !n) || new Set(names).size !== names.length) {
            setStatus({
                kind: "error",
                text: "Every polygon needs a unique, non-empty name.",
            });
            return;
        }

        setExporting(true);
        try {
            // Step 1: upload the background (only if it changed).
            let ref = imageRef;
            if (imageFile && !ref) {
                ref = await uploadMapImage(imageFile);
                setImageRef(ref);
            }

            // Step 2: send the map as JSON.
            const round = (n: number) => Math.round(n * 100) / 100;
            const payload: MapExportPayload = {
                mapId,
                name,
                imageRef: ref,
                width: size.width,
                height: size.height,
                polygons: polygons.map((poly) => ({
                    name: poly.name.trim(),
                    points: poly.points.map((p) => ({
                        x: round(p.x),
                        y: round(p.y),
                    })),
                })),
            };

            const result = await exportMap(payload);
            setMapId(result.mapId);
            setStatus({
                kind: "success",
                text: `Map saved (id ${result.mapId}).`,
            });
        } catch (error) {
            setStatus({
                kind: "error",
                text:
                    error instanceof Error
                        ? error.message
                        : "Something went wrong exporting the map.",
            });
        } finally {
            setExporting(false);
        }
    };

    /* ------------------------------------------------------------ render */

    const drawing = mode === "draw";
    const colorFor = (index: number) => COLORS[index % COLORS.length];

    return (
        <div className="map-maker">
            <h1>Create Your Map</h1>

            <div className="map-maker-controls">
                <input
                    type="text"
                    className="map-name-input"
                    placeholder="Map name"
                    value={mapName}
                    maxLength={100}
                    onChange={(e) => setMapName(e.target.value)}
                />

                <label className="file-button">
                    {imageUrl ? "Change background" : "Upload background (optional)"}
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageUpload}
                        hidden
                    />
                </label>

                {imageUrl && (
                    <button onClick={removeBackground}>Remove background</button>
                )}

                <div className="mode-toggle">
                    <button
                        className={drawing ? "active" : ""}
                        onClick={startDrawing}
                    >
                        Draw
                    </button>
                    <button
                        className={!drawing ? "active" : ""}
                        onClick={() => setMode("edit")}
                        disabled={draft.length > 0}
                    >
                        Edit
                    </button>
                </div>

                <button onClick={clearAll}>Clear all</button>
            </div>

            <div className="map-workspace">
                <div className="map-editor">
                    <svg
                        ref={svgRef}
                        className={`map-canvas ${drawing ? "drawing" : ""}`}
                        viewBox={`0 0 ${size.width} ${size.height}`}
                        onClick={handleCanvasClick}
                    >
                        <rect
                            width={size.width}
                            height={size.height}
                            fill="#f3f4f6"
                        />

                        {imageUrl && (
                            <image
                                href={imageUrl}
                                width={size.width}
                                height={size.height}
                                preserveAspectRatio="none"
                            />
                        )}

                        {/* Confirmed polygons */}
                        {polygons.map((poly, index) => {
                            const color = colorFor(index);
                            const selected = poly.id === selectedId;
                            const c = centroid(poly.points);

                            return (
                                <g key={poly.id}>
                                    <polygon
                                        points={toPointsAttr(poly.points)}
                                        fill={color}
                                        fillOpacity={selected ? 0.45 : 0.3}
                                        stroke={color}
                                        strokeWidth={selected ? 3 : 2}
                                        style={{
                                            cursor: "pointer",
                                            pointerEvents: drawing
                                                ? "none"
                                                : "auto",
                                        }}
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            setSelectedId(poly.id);
                                        }}
                                    />
                                    <text
                                        x={c.x}
                                        y={c.y}
                                        className="polygon-label"
                                    >
                                        {poly.name}
                                    </text>
                                </g>
                            );
                        })}

                        {/* Handles of the selected polygon (edit mode) */}
                        {!drawing &&
                            polygons
                                .filter((poly) => poly.id === selectedId)
                                .map((poly) =>
                                    poly.points.map((p, i) => (
                                        <circle
                                            key={`${poly.id}-${i}`}
                                            className="point-handle"
                                            cx={p.x}
                                            cy={p.y}
                                            r={7}
                                            onPointerDown={(e) =>
                                                startDrag(e, poly.id, i)
                                            }
                                            onPointerMove={onDrag}
                                            onPointerUp={endDrag}
                                            onPointerCancel={endDrag}
                                            onClick={(e) => e.stopPropagation()}
                                            onDoubleClick={() =>
                                                removePoint(poly.id, i)
                                            }
                                        />
                                    ))
                                )}

                        {/* Polygon currently being drawn */}
                        {draft.length > 0 && (
                            <>
                                <polygon
                                    points={toPointsAttr(draft)}
                                    fill={
                                        confirming
                                            ? "rgba(34, 197, 94, 0.3)"
                                            : "rgba(239, 68, 68, 0.15)"
                                    }
                                    stroke={confirming ? "#16a34a" : "#ef4444"}
                                    strokeWidth={2}
                                    strokeDasharray={confirming ? undefined : "6 4"}
                                    style={{ pointerEvents: "none" }}
                                />
                                {draft.map((p, i) => (
                                    <circle
                                        key={i}
                                        className="point-handle draft"
                                        cx={p.x}
                                        cy={p.y}
                                        r={7}
                                        onPointerDown={(e) =>
                                            startDrag(e, "draft", i)
                                        }
                                        onPointerMove={onDrag}
                                        onPointerUp={endDrag}
                                        onPointerCancel={endDrag}
                                        onClick={(e) => e.stopPropagation()}
                                        onDoubleClick={() =>
                                            removePoint("draft", i)
                                        }
                                    />
                                ))}
                            </>
                        )}
                    </svg>

                    <p className="map-hint">
                        {drawing
                            ? "Click to place points. Drag a point to move it, double-click to delete it."
                            : "Click a polygon to select it, then drag its points. Double-click a point to delete it."}
                    </p>
                </div>

                <aside className="map-sidebar">
                    {/* Draft controls / confirmation */}
                    {draft.length > 0 && (
                        <section className="panel">
                            <h2>New polygon</h2>
                            <p>{draft.length} point{draft.length === 1 ? "" : "s"}</p>

                            <input
                                type="text"
                                placeholder="Polygon name"
                                value={draftName}
                                maxLength={100}
                                onChange={(e) => {
                                    setDraftName(e.target.value);
                                    setConfirmError("");
                                }}
                                onKeyDown={(e) => {
                                    if (e.key === "Enter" && confirming) {
                                        confirmPolygon();
                                    }
                                }}
                            />

                            {confirming ? (
                                <>
                                    <p>Happy with this polygon?</p>
                                    <div className="button-row">
                                        <button onClick={confirmPolygon}>
                                            Yes, save polygon
                                        </button>
                                        <button
                                            onClick={() => setConfirming(false)}
                                        >
                                            No, keep editing
                                        </button>
                                    </div>
                                </>
                            ) : (
                                <div className="button-row">
                                    <button
                                        onClick={finishPolygon}
                                        disabled={draft.length < 3}
                                    >
                                        Finish polygon
                                    </button>
                                    <button onClick={undoPoint}>Undo point</button>
                                </div>
                            )}

                            <div className="button-row">
                                <button onClick={cancelDraft}>Cancel</button>
                            </div>

                            {confirmError && (
                                <p className="message error">{confirmError}</p>
                            )}
                        </section>
                    )}

                    {/* Confirmed polygons */}
                    <section className="panel">
                        <h2>Polygons ({polygons.length})</h2>

                        {polygons.length === 0 && (
                            <p className="empty">None yet. Click on the map to start one.</p>
                        )}

                        <ul className="polygon-list">
                            {polygons.map((poly, index) => (
                                <li
                                    key={poly.id}
                                    className={
                                        poly.id === selectedId ? "selected" : ""
                                    }
                                    onClick={() => selectPolygon(poly.id)}
                                >
                                    <span
                                        className="swatch"
                                        style={{ background: colorFor(index) }}
                                    />
                                    <input
                                        type="text"
                                        value={poly.name}
                                        maxLength={100}
                                        onClick={(e) => e.stopPropagation()}
                                        onFocus={() => selectPolygon(poly.id)}
                                        onChange={(e) =>
                                            renamePolygon(poly.id, e.target.value)
                                        }
                                    />
                                    <button
                                        title="Delete polygon"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            deletePolygon(poly.id);
                                        }}
                                    >
                                        ✕
                                    </button>
                                </li>
                            ))}
                        </ul>

                        {!drawing && draft.length === 0 && (
                            <button onClick={startDrawing}>+ New polygon</button>
                        )}
                    </section>

                    {/* Export */}
                    <section className="panel">
                        <button
                            className="export-button"
                            onClick={handleExport}
                            disabled={exporting}
                        >
                            {exporting
                                ? "Exporting…"
                                : mapId === null
                                  ? "Export map"
                                  : "Update map"}
                        </button>

                        {status && (
                            <p className={`message ${status.kind}`}>
                                {status.text}
                            </p>
                        )}
                    </section>
                </aside>
            </div>
        </div>
    );
}

export default MapMaker;
