import { useNavigate } from 'react-router-dom';
import AffectationForm from '../components/AffectationForm';
import { createAffectation } from '../api/affectationApi';

const AffectationCreatePage = () => {
  const navigate = useNavigate();

  const handleSubmit = async (data) => {
    await createAffectation(data);
  };

  return (
    <div className="form-page">
      <div className="form-page__header">
        <button type="button" className="form-page__back" onClick={() => navigate('/affectations')} aria-label="Retour">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polyline points="15 18 9 12 15 6" />
          </svg>
        </button>
        <div>
          <h1>Nouvelle affectation</h1>
          <p className="form-page__subtitle">Affectez un ouvrier à un chantier pour une période donnée</p>
        </div>
      </div>

      <AffectationForm onSubmit={handleSubmit} submitLabel="Créer l'affectation" isEdit={false} />
    </div>
  );
};

export default AffectationCreatePage;