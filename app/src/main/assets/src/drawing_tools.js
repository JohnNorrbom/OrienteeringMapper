// Globals
/* TODO:
    * Add styling to features
    * Optimize for bigger files (cant load big files)
    * Fix zoom (max zoom out 100m on scale bar)
    * Export files
*/
let drawMode = null;
let firstPolygonMarker = null;
let tempPoints = [];

window.map = L.map('map', {
    crs: L.CRS.Simple,
    center: [0, 0],
    zoom: 16,
    zoomControl: false,
    attributionControl: false
});
map.doubleClickZoom.disable();
L.control.scale().addTo(map);

const uploader = document.getElementById('uploader');
if (uploader) {
  uploader.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const arrayBuffer = await file.arrayBuffer();
    const ocadFile = await window.readOcad(arrayBuffer);
    const geojson = window.ocadToGeoJson(ocadFile);
    const layer = L.geoJSON(geojson).addTo(map);
    map.fitBounds(layer.getBounds());
  });
}

window.loadOcadFromBase64 = async function (b64) {
  try {
    const binary = atob(b64);
    const len    = binary.length;
    const bytes  = new Uint8Array(len);
    for (let i = 0; i < len; i++) bytes[i] = binary.charCodeAt(i);
    const buffer = Buffer.from(bytes);
    const ocadFile = await window.readOcad(buffer);
    const geojson  = await window.ocadToGeoJson(ocadFile);
    const features = geojson.features || [];
    const chunkSize = 200;
    let cursor = 0;
    function addChunk() {
      const chunk = features.slice(cursor, cursor + chunkSize);
      if (chunk.length > 0) {
        L.geoJSON(
          { type: 'FeatureCollection', features: chunk },
          { style: {/* optional styling */} }
        ).addTo(map);
        cursor += chunkSize;
        setTimeout(addChunk, 50);
      } else {
        map.fitBounds(L.geoJSON(geojson).getBounds());
      }
    }
    addChunk();
  } catch (err) {
    console.error('Error loading OCAD:', err);
  }
};

window.toggleMode = function (mode) {
  if (drawMode === mode) {
    if (drawMode === 'polygon') {
      L.polygon(tempPoints, { color: '#f8ab0f' }).addTo(map);
    }
    drawMode = null;
    tempPoints = [];
  } else {
    if (drawMode === 'polygon') {
      L.polygon(tempPoints, { color: '#f8ab0f', fillOpacity: 1 }).addTo(map);
    }
    if (firstPolygonMarker) {
      map.removeLayer(firstPolygonMarker);
      firstPolygonMarker = null;
    }
    drawMode = mode;
    tempPoints = [];
  }
};

// Map click logic
map.on('click', function (e) {
  const latlng = e.latlng;
  if (!drawMode) return;

  switch (drawMode) {
    case 'point':
      L.circle([latlng.lat, latlng.lng], {
        radius: 5,
        color: 'black',
        fillColor: 'black',
        fillOpacity: 1,
        weight: 1
      }).addTo(map);
      break;
    case 'line':
      tempPoints.push(latlng);
      if (tempPoints.length >= 2) {
        L.polyline(tempPoints, {
          color: 'black',
          weight: 2,
          dashArray: '8, 8',
          lineCap: 'butt'
        }).addTo(map);
      }
      break;
    case 'polygon':
      tempPoints.push(latlng);
      if (tempPoints.length === 1) {
        firstPolygonMarker = L.circleMarker([latlng.lat, latlng.lng], {
          radius: 4,
          color: '#f8ab0f',
          fillColor: '#f8ab0f',
          fillOpacity: 1,
          weight: 1
        }).addTo(map);
      }
      if (tempPoints.length >= 2) {
        L.polyline(tempPoints, { color: '#f8ab0f' }).addTo(map);
      }
      break;
    case 'triangle':
      const offset = 0.0003;
      const triangle = [
        [latlng.lat + offset, latlng.lng],
        [latlng.lat - offset, latlng.lng - offset],
        [latlng.lat - offset, latlng.lng + offset]
      ];
      L.polygon(triangle, {
        color: 'purple',
        fillColor: 'none',
        fillOpacity: 0.0
      }).addTo(map);
      break;
  }
});
