import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import '../lib/leafletIconFix';

// Centre par défaut tant qu'aucune position n'a été trouvée (Marrakech)
const DEFAULT_CENTER = [31.6295, -7.9811];

const RecenterMap = ({ center }) => {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.setView(center, 15);
    }
  }, [center, map]);
  return null;
};

/**
 * Sélecteur d'emplacement : géocode une adresse via Nominatim (OpenStreetMap)
 * puis affiche un marqueur déplaçable pour ajuster la position exacte.
 *
 * Props :
 *  - address     : adresse texte à géocoder (déjà saisie dans le formulaire parent)
 *  - latitude    : number | null
 *  - longitude   : number | null
 *  - onChange(lat, lng) : appelé quand la position est trouvée ou déplacée
 */
const LocationPicker = ({ address, latitude, longitude, onChange }) => {
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');

  const hasCoords = latitude != null && longitude != null;
  const position = hasCoords ? [latitude, longitude] : DEFAULT_CENTER;

  const handleGeocode = async () => {
    if (!address || !address.trim()) {
      setSearchError("Renseigne d'abord une adresse ci-dessus.");
      return;
    }
    setSearching(true);
    setSearchError('');
    try {
      const url = `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(address)}`;
      const res = await fetch(url);
      const results = await res.json();

      if (!results || results.length === 0) {
        setSearchError("Adresse introuvable. Essaie de la préciser (ville, quartier...).");
        return;
      }

      const { lat, lon } = results[0];
      onChange(parseFloat(lat), parseFloat(lon));
    } catch (err) {
      setSearchError('Erreur lors de la géolocalisation. Réessaie.');
    } finally {
      setSearching(false);
    }
  };

  const handleDragEnd = (e) => {
    const { lat, lng } = e.target.getLatLng();
    onChange(lat, lng);
  };

  return (
    <div className="form-group">
      <label>Emplacement du chantier</label>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 10, flexWrap: 'wrap' }}>
        <button
          type="button"
          className="btn-view"
          style={{ flex: 'none' }}
          onClick={handleGeocode}
          disabled={searching}
        >
          {searching ? 'Recherche...' : '📍 Localiser à partir de l\'adresse'}
        </button>
        {hasCoords && (
          <span style={{ fontSize: 13, color: '#6b7280' }}>
            {latitude.toFixed(5)}, {longitude.toFixed(5)}
          </span>
        )}
      </div>

      {searchError && (
        <div className="error-banner" style={{ marginBottom: 10 }}>❌ {searchError}</div>
      )}

      <div className="map-picker">
        <MapContainer
          center={position}
          zoom={hasCoords ? 15 : 6}
          scrollWheelZoom
          style={{ height: '260px', width: '100%' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <RecenterMap center={hasCoords ? position : null} />
          {hasCoords && (
            <Marker position={position} draggable eventHandlers={{ dragend: handleDragEnd }} />
          )}
        </MapContainer>
      </div>

      {hasCoords && (
        <p style={{ fontSize: 12, color: '#9ca3af', marginTop: 6 }}>
          Tu peux ajuster le point en le faisant glisser sur la carte.
        </p>
      )}
    </div>
  );
};

export default LocationPicker;