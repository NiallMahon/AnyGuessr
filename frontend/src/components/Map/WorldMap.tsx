import { useState } from "react";
import { recordGuess, startGame } from "../../services/statsAPI";

const DEMO_USER_ID = 1;
const DEMO_MAP_ID = 1;
const TARGET_REGION_ID = 1;

function WorldMap() {
  const [selected, setSelected] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  async function guess(regionId: number) {
    // clicks made while the last one is still being sent are ignored, so the game
    // is only started once and guesses reach the server in the order they were made
    if (sending) {
      return;
    }

    setSending(true);

    try {
      let id = sessionId;

      if (!id) {
        id = crypto.randomUUID();
        await startGame(id, DEMO_USER_ID, DEMO_MAP_ID);
        setSessionId(id);
      }

      await recordGuess(id, {
        targetRegionId: TARGET_REGION_ID,
        selectedRegionId: regionId,
        correct: regionId === TARGET_REGION_ID,
        lat: null,
        lng: null,
      });
    } finally {
      setSending(false);
    }
  }

  return (
    <div>
      <h2>
        Selected: {selected ?? "Nothing"}
      </h2>

      <svg
        width="800"
        height="500"
        viewBox="0 0 800 500"
        style={{ border: "1px solid black" }}
      >
        {/* Polygon 1 */}
        <polygon
          points="100,150 200,100 300,130 350,200 300,300 200,350 120,280"
          fill="lightblue"
          stroke="black"
          strokeWidth="2"
          onClick={() => { setSelected("Country A"); guess(1); }}
          style={{ cursor: "pointer" }}
        />

        {/* Polygon 2 */}
        <polygon
          points="450,150 550,100 650,150 680,250 620,350 500,330 430,250"
          fill="lightgreen"
          stroke="black"
          strokeWidth="2"
          onClick={() => { setSelected("Country B"); guess(2); }}
          style={{ cursor: "pointer" }}
        />
      </svg>
    </div>
  );
}

export default WorldMap;
