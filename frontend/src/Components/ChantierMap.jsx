import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import '../lib/leafletIconFix';

/**
 * Affiche l'emplacement d'un chantier sur une carte (lecture seule).
 * Ne rend rien si le chantier n'a pas de coordonnées enregistrées.
 */
const ChantierMap = ({ latitude, longitude, name }) => {
  if (latitude == null || longitude == null) return null;

  const position = [latitude, longitude];

  return (
    <div className="chantier-card chantier-map" style={{ maxWidth: 620, marginBottom: 20, padding: 0, overflow: 'hidden' }}>
      <MapContainer center={position} zoom={15} scrollWheelZoom={false} style={{ height: '280px', width: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={position}>
          <Popup>{name}</Popup>
        </Marker>
      </MapContainer>
    </div>
  );
};

export default ChantierMap;