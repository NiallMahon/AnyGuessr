const API_BASE: string =
    (import.meta.env.VITE_API_URL as string | undefined) ??
    "http://localhost:8080";

export type Point = {
    x: number;
    y: number;
};

export type PolygonPayload = {
    name: string;
    points: Point[];
};

export type MapExportPayload = {
    /** null when creating a new map; the backend assigns and returns the id. */
    mapId: number | null;
    name: string;
    /** Reference returned by the image upload endpoint, or null for no background. */
    imageRef: string | null;
    /** Size of the coordinate space the polygon points live in. */
    width: number;
    height: number;
    polygons: PolygonPayload[];
};

async function readError(response: Response, fallback: string) {
    try {
        const body = await response.json();
        if (body && typeof body.message === "string") return body.message;
    } catch {
        /* response was not JSON */
    }
    return `${fallback} (HTTP ${response.status})`;
}

/** Uploads the background image and returns the backend's reference to it. */
export async function uploadMapImage(file: File): Promise<string> {
    const form = new FormData();
    form.append("image", file);

    const response = await fetch(`${API_BASE}/api/maps/images`, {
        method: "POST",
        body: form,
    });

    if (!response.ok) {
        throw new Error(await readError(response, "Image upload failed"));
    }

    const data = (await response.json()) as { imageRef: string };
    return data.imageRef;
}

/** Sends the map JSON to the backend. Returns the (possibly new) mapId. */
export async function exportMap(
    payload: MapExportPayload
): Promise<{ mapId: number }> {
    const response = await fetch(`${API_BASE}/api/maps`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
    });

    if (!response.ok) {
        throw new Error(await readError(response, "Map export failed"));
    }

    return (await response.json()) as { mapId: number };
}
