import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { startQuiz } from "../services/gameplayAPI";
import "./MapSelector.css";

function MapSelector() {
  const navigate = useNavigate();

  const [startingMapId, setStartingMapId] =
    useState<number | null>(null);

  const [error, setError] =
    useState<string | null>(null);

  const maps = [
    {
      id: 1,
      name: "World",
      description:
        "Test your knowledge of countries around the world.",
      image:
        "https://img.magnific.com/premium-vector/world-map-flat-design-vector-illustration_600323-2501.jpg?semt=ais_hybrid&w=740&q=80",
      difficulty: "Easy",
      available: true,
    },
    {
      id: 2,
      name: "Europe",
      description:
        "Can you identify every country in Europe?",
      image:
        "https://opengameart.org/sites/default/files/Preview-EuropeMap1.jpg",
      difficulty: "Medium",
      available: false,
    },
    {
      id: 3,
      name: "Engine",
      description:
        "Identify different parts of an engine.",
      image:
        "https://img.magnific.com/premium-vector/blueprint-internal-combustion-engine-technical-drawing-blue-background-project_175654-201.jpg",
      difficulty: "Medium",
      available: false,
    },
  ];

  async function selectMap(
    mapId: number
  ) {
    setStartingMapId(mapId);
    setError(null);

    // Set by the login and register pages.
    const userId =
      sessionStorage.getItem("userId");

    try {
      const quiz =
        await startQuiz(
          String(mapId),
          userId === null
            ? null
            : Number(userId)
        );

      navigate("/quiz", {
        state: {
          sessionId:
            quiz.sessionId,

          mapId:
            quiz.mapId,

          initialTarget:
            quiz.currentTarget,
        },
      });

    } catch (err) {
      console.error(err);

      setError(
        "Unable to start the quiz."
      );

      setStartingMapId(null);
    }
  }

  return (
    <div className="map-selector">

      <div className="map-selector-header">
        <h1>Choose a Map</h1>

        <p>
          Select a map to start playing
        </p>

        {error && (
          <p>{error}</p>
        )}
      </div>

      <div className="map-grid">

        {maps.map((map) => (
          <div
            className="map-card"
            key={map.id}
            onClick={() => {
              if (
                map.available &&
                startingMapId === null
              ) {
                void selectMap(
                  map.id
                );
              }
            }}
            style={{
              cursor: map.available
                ? "pointer"
                : "not-allowed",

              opacity: map.available
                ? 1
                : 0.6,
            }}
          >
            <div className="map-image-container">
              <img
                src={map.image}
                alt={map.name}
              />
            </div>

            <div className="map-card-content">
              <h2>{map.name}</h2>

              <p>
                {map.description}
              </p>

              <div className="map-card-footer">
                <span className="difficulty">
                  {map.difficulty}
                </span>

                <span>
                  {!map.available
                    ? "Coming soon"
                    : startingMapId === map.id
                    ? "Starting..."
                    : "Play"}
                </span>
              </div>
            </div>
          </div>
        ))}

      </div>
    </div>
  );
}

export default MapSelector;