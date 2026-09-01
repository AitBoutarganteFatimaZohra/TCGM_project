import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import useTravaux from '../hooks/useTravaux';
import useChantiers from '../hooks/useChantiers';

const toLocalDateTime = (dateStr) =>
  dateStr ? `${dateStr}T00:00:00` : null;

const toDateInput = (isoString) =>
  isoString ? isoString.slice(0, 10) : '';

const toNullableLong = (value) =>
  value ? Number(value) : null;

const toNullableDecimal = (value) =>
  value ? Number(value) : null;

const TravauxEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const {
    fetchTravauxById,
    editTravaux,
    loading,
    error,
  } = useTravaux();

  const { chantiers } = useChantiers();

  const [form, setForm] = useState(null);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const t = await fetchTravauxById(id);

        setForm({
          code: t.code || '',
          intitule: t.intitule || '',
          description: t.description || '',
          dateDebutPrevue: toDateInput(
            t.dateDebutPrevue
          ),
          dateFinPrevue: toDateInput(
            t.dateFinPrevue
          ),
          priorite: t.priorite ?? '',
          statut: t.statut || 'PLANIFIE',
          budgetEstime: t.budgetEstime ?? '',
          chantierId: t.chantier?.id || '',
        });
      } catch (err) {
        setNotFound(true);
      }
    };

    load();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      code: form.code,
      intitule: form.intitule,
      description: form.description || null,
      dateDebutPrevue: toLocalDateTime(
        form.dateDebutPrevue
      ),
      dateFinPrevue: toLocalDateTime(
        form.dateFinPrevue
      ),
      priorite: toNullableLong(form.priorite),
      statut: form.statut,
      budgetEstime: toNullableDecimal(
        form.budgetEstime
      ),
      chantierId: toNullableLong(form.chantierId),
    };

    try {
      await editTravaux(id, payload);

      navigate(`/travaux/${id}`);
    } catch (err) {
      // L'erreur est déjà exposée via `error`
    }
  };

  /* =====================================================
     NOT FOUND
     ===================================================== */

  if (notFound) {
    return (
      <div className="travaux-edit-page">

        <div className="travaux-edit-empty">

          <div className="edit-empty-icon">
            🔧
          </div>

          <h2>Travaux introuvables</h2>

          <p>
            Les travaux demandés n'existent pas ou
            ne sont plus disponibles.
          </p>

          <Link
            to="/travaux"
            className="travaux-btn-primary"
          >
            ← Retour aux travaux
          </Link>

        </div>

      </div>
    );
  }

  /* =====================================================
     LOADING
     ===================================================== */

  if (!form) {
    return (
      <div className="travaux-loading">
        <div className="travaux-spinner"></div>
        <p>Chargement des travaux...</p>
      </div>
    );
  }

  return (
    <div className="travaux-edit-page">

      {/* =================================================
          BREADCRUMB
          ================================================= */}

      <div className="travaux-breadcrumb">

        <Link to="/travaux">
          Travaux
        </Link>

        <span>›</span>

        <Link to={`/travaux/${id}`}>
          {form.code}
        </Link>

        <span>›</span>

        <span>
          Modifier
        </span>

      </div>


      {/* =================================================
          HEADER
          ================================================= */}

      <div className="travaux-edit-header">

        <div className="edit-title-wrapper">

          <div className="edit-title-icon">
            ✏️
          </div>

          <div>
            <div className="edit-subtitle">
              {form.code}
            </div>

            <h1>
              Modifier les travaux
            </h1>

            <p>
              Mettez à jour les informations et le
              planning de ces travaux.
            </p>
          </div>

        </div>

        <Link
          to={`/travaux/${id}`}
          className="edit-back-btn"
        >
          ← Retour aux détails
        </Link>

      </div>


      {/* =================================================
          ERROR
          ================================================= */}

      {error && (
        <div className="travaux-error">

          <span className="error-icon">
            !
          </span>

          <div>
            <strong>
              Impossible d'enregistrer les modifications
            </strong>

            <p>
              {error}
            </p>
          </div>

        </div>
      )}


      {/* =================================================
          FORM
          ================================================= */}

      <form
        onSubmit={handleSubmit}
        className="travaux-edit-form"
      >

        {/* =================================================
            INFORMATIONS GENERALES
            ================================================= */}

        <div className="edit-form-card">

          <div className="edit-card-header">

            <div className="edit-card-icon">
              📋
            </div>

            <div>
              <h2>
                Informations générales
              </h2>

              <p>
                Identifiez les travaux et leur
                chantier.
              </p>
            </div>

          </div>


          <div className="edit-form-body">

            <div className="edit-form-row">

              {/* CODE */}

              <div className="edit-form-group">

                <label htmlFor="code">
                  Code
                  <span className="required">*</span>
                </label>

                <div className="edit-input-wrapper">

                  <span className="edit-input-icon">
                    #
                  </span>

                  <input
                    id="code"
                    type="text"
                    name="code"
                    value={form.code}
                    onChange={handleChange}
                    placeholder="Ex : TRV-001"
                    required
                  />

                </div>

                <span className="field-help">
                  Identifiant unique des travaux.
                </span>

              </div>


              {/* INTITULE */}

              <div className="edit-form-group">

                <label htmlFor="intitule">
                  Intitulé
                  <span className="required">*</span>
                </label>

                <input
                  id="intitule"
                  type="text"
                  name="intitule"
                  value={form.intitule}
                  onChange={handleChange}
                  placeholder="Ex : Travaux de fondation"
                  required
                />

              </div>

            </div>


            {/* CHANTIER */}

            <div className="edit-form-group">

              <label htmlFor="chantierId">
                Chantier
                <span className="required">*</span>
              </label>

              <div className="edit-input-wrapper">

                <span className="edit-input-icon">
                  🏗️
                </span>

                <select
                  id="chantierId"
                  name="chantierId"
                  value={form.chantierId}
                  onChange={handleChange}
                  required
                >

                  <option value="">
                    — Sélectionner un chantier —
                  </option>

                  {chantiers.map((c) => (
                    <option
                      key={c.id}
                      value={c.id}
                    >
                      {c.name}
                      {c.reference
                        ? ` (${c.reference})`
                        : ''}
                    </option>
                  ))}

                </select>

              </div>

            </div>


            {/* DESCRIPTION */}

            <div className="edit-form-group">

              <label htmlFor="description">
                Description
              </label>

              <textarea
                id="description"
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={5}
                placeholder="Décrivez les travaux à réaliser..."
              />

              <span className="field-help">
                Ajoutez les informations importantes
                concernant ces travaux.
              </span>

            </div>

          </div>

        </div>


        {/* =================================================
            PLANNING
            ================================================= */}

        <div className="edit-form-card">

          <div className="edit-card-header">

            <div className="edit-card-icon planning">
              📅
            </div>

            <div>
              <h2>
                Planning
              </h2>

              <p>
                Définissez les dates prévisionnelles
                des travaux.
              </p>
            </div>

          </div>


          <div className="edit-form-body">

            <div className="edit-form-row">

              <div className="edit-form-group">

                <label htmlFor="dateDebutPrevue">
                  Date de début prévue
                </label>

                <div className="edit-input-wrapper">

                  <span className="edit-input-icon">
                    📅
                  </span>

                  <input
                    id="dateDebutPrevue"
                    type="date"
                    name="dateDebutPrevue"
                    value={form.dateDebutPrevue}
                    onChange={handleChange}
                  />

                </div>

              </div>


              <div className="edit-form-group">

                <label htmlFor="dateFinPrevue">
                  Date de fin prévue
                </label>

                <div className="edit-input-wrapper">

                  <span className="edit-input-icon">
                    📅
                  </span>

                  <input
                    id="dateFinPrevue"
                    type="date"
                    name="dateFinPrevue"
                    value={form.dateFinPrevue}
                    onChange={handleChange}
                  />

                </div>

              </div>

            </div>

          </div>

        </div>


        {/* =================================================
            BUDGET / PRIORITE
            ================================================= */}

        <div className="edit-form-card">

          <div className="edit-card-header">

            <div className="edit-card-icon budget">
              💰
            </div>

            <div>
              <h2>
                Budget et priorité
              </h2>

              <p>
                Définissez le niveau de priorité et
                le budget estimé.
              </p>
            </div>

          </div>


          <div className="edit-form-body">

            <div className="edit-form-row">

              {/* PRIORITE */}

              <div className="edit-form-group">

                <label htmlFor="priorite">
                  Priorité
                </label>

                <div className="priority-input-container">

                  <input
                    id="priorite"
                    type="number"
                    name="priorite"
                    min="1"
                    max="5"
                    value={form.priorite}
                    onChange={handleChange}
                    placeholder="1 à 5"
                  />

                  <div className="priority-scale">

                    <span>1</span>
                    <span>2</span>
                    <span>3</span>
                    <span>4</span>
                    <span>5</span>

                  </div>

                </div>

                <span className="field-help">
                  1 = faible · 5 = très élevée
                </span>

              </div>


              {/* BUDGET */}

              <div className="edit-form-group">

                <label htmlFor="budgetEstime">
                  Budget estimé
                </label>

                <div className="edit-input-wrapper budget-input">

                  <input
                    id="budgetEstime"
                    type="number"
                    name="budgetEstime"
                    step="0.01"
                    min="0"
                    value={form.budgetEstime}
                    onChange={handleChange}
                    placeholder="0.00"
                  />

                  <span className="currency">
                    DH
                  </span>

                </div>

              </div>

            </div>

          </div>

        </div>


        {/* =================================================
            STATUT
            ================================================= */}

        <div className="edit-form-card">

          <div className="edit-card-header">

            <div className="edit-card-icon status">
              📊
            </div>

            <div>
              <h2>
                Statut
              </h2>

              <p>
                Indiquez l'état actuel des travaux.
              </p>
            </div>

          </div>


          <div className="edit-form-body">

            <div className="status-edit-options">

              {[
                {
                  value: 'PLANIFIE',
                  label: 'Planifié',
                  icon: '📅',
                },
                {
                  value: 'EN_COURS',
                  label: 'En cours',
                  icon: '🔄',
                },
                {
                  value: 'TERMINE',
                  label: 'Terminé',
                  icon: '✓',
                },
                {
                  value: 'SUSPENDU',
                  label: 'Suspendu',
                  icon: '⏸',
                },
              ].map((status) => (

                <label
                  key={status.value}
                  className={`status-edit-option status-option-${status.value.toLowerCase()} ${
                    form.statut === status.value
                      ? 'status-edit-option-active'
                      : ''
                  }`}
                >

                  <input
                    type="radio"
                    name="statut"
                    value={status.value}
                    checked={
                      form.statut === status.value
                    }
                    onChange={handleChange}
                  />

                  <span className="status-option-icon">
                    {status.icon}
                  </span>

                  <span className="status-option-text">
                    {status.label}
                  </span>

                  {form.statut === status.value && (
                    <span className="status-option-check">
                      ✓
                    </span>
                  )}

                </label>

              ))}

            </div>

          </div>

        </div>


        {/* =================================================
            ACTIONS
            ================================================= */}

        <div className="edit-form-actions">

          <button
            type="button"
            className="edit-cancel-btn"
            onClick={() =>
              navigate(`/travaux/${id}`)
            }
            disabled={loading}
          >
            Annuler
          </button>

          <button
            type="submit"
            className="edit-save-btn"
            disabled={loading}
          >

            {loading ? (
              <>
                <span className="button-spinner"></span>
                Enregistrement...
              </>
            ) : (
              <>
                ✓
                Enregistrer les modifications
              </>
            )}

          </button>

        </div>

      </form>

    </div>
  );
};

export default TravauxEditPage;