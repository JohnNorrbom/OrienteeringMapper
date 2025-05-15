let drawMode = null;
    let tempPoints = [];

    // Leaflet map setup
    var map = L.map('map').setView([51.505, -0.09], 13);
    map.doubleClickZoom.disable();
    map.removeControl(map.zoomControl);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

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
                L.polygon(tempPoints, { color: 'green' }).addTo(map);
            }
            // If same button clicked again, turn off
            drawMode = null;
            tempPoints = [];
        } else {
            if(drawMode == 'polygon') {
                L.polygon(tempPoints, { color: 'green' }).addTo(map);
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
                L.marker(latlng).addTo(map);
                break;
            case 'line':
                tempPoints.push(latlng);
                if (tempPoints.length >= 2) {
                    L.polyline(tempPoints, { color: 'blue' }).addTo(map);
                }
                break;

            case 'polygon':
                tempPoints.push(latlng);
                if (tempPoints.length <= 1) {
                    L.marker(latlng).addTo(map);
                }
                if (tempPoints.length >= 2) {
                    L.polyline(tempPoints, { color: 'green' }).addTo(map);
                }
                break;

            case 'triangle':
                // When triangle mode is active, place a small triangle at clicked location
                const offset = 0.001; // triangle size
                const triangle = [
                    [latlng.lat + offset, latlng.lng],
                    [latlng.lat - offset, latlng.lng - offset],
                    [latlng.lat - offset, latlng.lng + offset]
                ];
                L.polygon(triangle, {
                    color: 'purple',
                    fillColor: 'purple',
                    fillOpacity: 0.6
                }).addTo(map);
                break;
        }
    });