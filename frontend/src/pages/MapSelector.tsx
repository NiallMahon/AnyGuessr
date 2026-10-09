
import './MapSelector.css';


function MapSelector() {
  // Temporary static data.
  // Later, this will come from the backend.
  const maps = [
    {
      id: 1,
      name: 'World',
      description: 'Test your knowledge of countries around the world.',
      image: 'https://img.magnific.com/premium-vector/world-map-flat-design-vector-illustration_600323-2501.jpg?semt=ais_hybrid&w=740&q=80',
      difficulty: 'Easy'
    },
    {
      id: 2,
      name: 'Europe',
      description: 'Can you identify every country in Europe?',
      image: 'https://opengameart.org/sites/default/files/Preview-EuropeMap1.jpg',
      difficulty: 'Medium'
    },
    {
      id: 3,
      name: 'Engnine',
      description: 'Identify all 50 states.',
      image: 'https://img.magnific.com/premium-vector/blueprint-internal-combustion-engine-technical-drawing-blue-background-project_175654-201.jpg',
      difficulty: 'Medium'
    }
  ];

  const selectMap = (mapId: number) => {
    console.log('Selected map:', mapId);
  };

  return (
    <div className="map-selector">

      <div className="map-selector-header">
        <h1>Choose a Map</h1>
        <p>Select a map to start playing</p>
      </div>

      <div className="map-grid">

        {maps.map((map) => (
          <div
            className="map-card"
            key={map.id}
            onClick={() => selectMap(map.id)}
          >

            <div className="map-image-container">
              <img src={map.image} alt={map.name} />
            </div>

            <div className="map-card-content">
              <h2>{map.name}</h2>

              <p>{map.description}</p>

              <div className="map-card-footer">
                <span className="difficulty">
                  {map.difficulty}
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
