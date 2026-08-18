import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import AffectationForm from '../components/AffectationForm';
import { getAffectationById, updateAffectation } from '../api/affectationApi';

const AffectationEditPage = () => {
  const { id } = useParams();
  const [affectation, setAffectation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAffectationById(id)
      .then(setAffectation)
      .catch(() => setError('Impossible de charger cette affectation.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (data) => {
    await updateAffectation(id, data);
  };

  if (loading) return <div className="loading">Chargement...</div>;
  if (error) return <div className="error-banner">{error}</div>;

  return (
    <div className="affectation-edit-page">
      <div className="page-header">
        <h1>Modifier l'affectation</h1>
      </div>
      <AffectationForm
        initialData={affectation}
        onSubmit={handleSubmit}
        submitLabel="Enregistrer les modifications"
        isEdit={true}
      />
    </div>
  );
};

export default AffectationEditPage;