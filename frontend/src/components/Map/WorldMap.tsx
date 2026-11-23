import { useState } from "react";

function WorldMap() {
  const [selected, setSelected] = useState<string | null>(null);

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
          onClick={() => setSelected("Country A")}
          style={{ cursor: "pointer" }}
        />

        {/* Polygon 2 */}
        <polygon
          points="450,150 550,100 650,150 680,250 620,350 500,330 430,250"
          fill="lightgreen"
          stroke="black"
          strokeWidth="2"
          onClick={() => setSelected("Country B")}
          style={{ cursor: "pointer" }}
        />
      </svg>
    </div>
  );
}

export default WorldMap;