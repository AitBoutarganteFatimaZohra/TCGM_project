import { useEffect, useState } from 'react';
import { getMySites } from '../api/chantierApi';

// Renvoie les chantiers où l'utilisateur connecté est affecté
// (le backend filtre déjà selon son rôle : magasinier, chef de chantier, etc.)
const useMySites = () => {
  const [sites, setSites] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    getMySites({ size: 50 }) // taille de page large : un utilisateur a rarement +50 sites
      .then((data) => setSites(data?.content || []))
      .finally(() => setLoading(false));
  }, []);

  return { sites, loading };
};

export default useMySites;