let drawMode = null;
let firstPolygonMarker = null;
let tempPoints = [];
// Leaflet map setup
const map = L.map('map', {
    center: [0, 0],
    zoom: 8,
    zoomControl: false,
    attributionControl: false
});
map.doubleClickZoom.disable();

// Button function handlers (called from Android via WebView)
function addPoint() {
    toggleMode('point');
}

function addLine() {
    toggleMode('line');
}

function addPolygon() {
    toggleMode('polygon');
}

function addTriangle() {
    toggleMode('triangle');
}

function toggleMode(mode) {
    if (drawMode === mode) {
        if(drawMode == 'polygon') {
            L.polygon(tempPoints, { color: '#f8ab0f' }).addTo(map);
        }
        // If same button clicked again, turn off
        drawMode = null;
        tempPoints = [];
    } else {
        if(drawMode == 'polygon') {
            L.polygon(tempPoints, { color: '#f8ab0f', fillOpacity: 1 }).addTo(map);
            // TODO: remove all lines too
        }
        if (firstPolygonMarker) {
            map.removeLayer(firstPolygonMarker);
            firstPolygonMarker = null;
        }
        // Switch mode and clear previous points
        drawMode = mode;
        tempPoints = [];
    }
}

// Map click event
map.on('click', function (e) {
const latlng = e.latlng;
if (!drawMode) return;

switch (drawMode) {
    case 'point':
        L.circleMarker([latlng.lat, latlng.lng], {
            radius: 4,           // ~4px to approximate a 0.4mm symbol at standard display DPI
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
                weight: 2,        // pixel width of the line (~0.25 mm print equivalent)
                dashArray: '8, 8', // dash (px), gap (px)
                lineCap: 'butt',   // no rounding at the end
            }).addTo(map);
        }
        break;
    case 'polygon':
        // 401 Open land
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
            const offset = 1; // triangle size
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