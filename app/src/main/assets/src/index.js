const { readOcad, ocadToGeoJson } = require('ocad2geojson');
require('./drawing_tools.js');

window.readOcad = readOcad;
window.ocadToGeoJson = ocadToGeoJson;
