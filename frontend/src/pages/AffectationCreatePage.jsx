import { useNavigate } from 'react-router-dom';
import AffectationForm from '../components/AffectationForm';
import { createAffectation } from '../api/affectationApi';
import { affecterOuvrier } from '../api/tacheApi';

const AffectationCreatePage = () => {
  const navigate = useNavigate();

  const handleSubmit = async (data) => {
    // ⚠️ NOUVEAU : tacheId n'appartient pas à l'entité Affectation — on
    // l'extrait avant d'envoyer le payload à createAffectation, puis on
    // affecte séparément l'ouvrier à la tâche si une tâche a été choisie.
    const { tacheId, ...affectationPayload } = data;

    await createAffectation(affectationPayload);

    if (tacheId) {
      try {
        await affecterOuvrier(tacheId, affectationPayload.ouvrierId);
      } catch (err) {
        // L'affectation au chantier a réussi ; seule l'affectation à la
        // tâche a échoué (ex: déjà affecté à cette tâche). On ne bloque
        // pas la redirection pour autant, mais on prévient l'utilisateur.
        alert(
          err?.response?.data?.message ||
            "L'affectation au chantier a été créée, mais l'affectation à la tâche a échoué."
        );
      }
    }
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