
import { useEffect, useState } from "react";
import {
  getMapDetails,
  getMapImageUrl,
  type MapDetail,
} from "../../api/maps";

type QuizMapProps = {
  mapId: string;
  targetRegionId: string;
  onRegionClick: (regionId: string) => void;
  disabled: boolean;
};

/**
 * Displays a saved map and submits clicks on its regions.
 */
function QuizMap({
  mapId,
  targetRegionId,
  onRegionClick,
  disabled,
}: QuizMapProps) {
  const [map, setMap] = useState<MapDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    getMapDetails(mapId)
      .then((data) => {
        if (active) {
          setMap(data);
        }
      })
      .catch((err) => {
        console.error("Unable to load quiz map:", err);
        if (active) {
          setError("Unable to load the selected map.");
        }
      });

    return () => {
      active = false;
    };
  }, [mapId]);

  if (error) {
    return <p role="alert">{error}</p>;
  }

  if (!map) {
    return <p>Loading quiz map...</p>;
  }

  if (map.width <= 0 || map.height <= 0) {
    return <p role="alert">Invalid map dimensions.</p>;
  }

  const regions = map.regions.filter(
    (region) => Array.isArray(region.points) &&
      region.points.length >= 3
  );

  if (regions.length === 0) {
    return (
      <p role="alert">
        This map has no drawable regions.
      </p>
    );
  }

const targetRegion = regions.find(
  (region) => String(region.id) === targetRegionId
);

if (!targetRegion) {
  return (
    <p role="alert">
      The current quiz target has no drawable polygon.
    </p>
  );
}

  const imageUrl = getMapImageUrl(map.imageRef);

  function selectRegion(regionId: number) {
    if (!disabled) {
      onRegionClick(String(regionId));
    }
  }

  return (
    <section aria-label={`${map.name} quiz map`}>
      <h3>{map.name}</h3>
    <h2>Find region: {targetRegion.name}</h2>

      <svg
        viewBox={`0 0 ${map.width} ${map.height}`}
        role="group"
        aria-label={`Clickable regions for ${map.name}`}
        style={{
          width: "100%",
          maxWidth: 900,
          height: "auto",
          border: "1px solid #aaa",
          background: "#f8fafc",
        }}
      >
        {imageUrl && (
          <image
            href={imageUrl}
            width={map.width}
            height={map.height}
            preserveAspectRatio="none"
          />
        )}

        {regions.map((region) => (
          <polygon
            key={region.id}
            points={region.points
              .map((point) => `${point.x},${point.y}`)
              .join(" ")}
            fill="#2563eb"
            fillOpacity={0.2}
            stroke="#1d4ed8"
            strokeWidth={2}
            role="button"
            tabIndex={disabled ? -1 : 0}
            aria-label={`Select ${region.name}`}
            onClick={() => selectRegion(region.id)}
            onKeyDown={(event) => {
              if (
                event.key === "Enter" ||
                event.key === " "
              ) {
                event.preventDefault();
                selectRegion(region.id);
              }
            }}
            style={{
              cursor: disabled
                ? "not-allowed"
                : "pointer",
            }}
          />
        ))}
      </svg>
    </section>
  );
}

export default QuizMap;
