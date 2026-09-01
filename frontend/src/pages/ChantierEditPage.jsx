import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';
import useAuth from '../hooks/useAuth';
import LocationPicker from '../components/LocationPicker';
import { getClients } from '../api/clientApi';
import { getUsersByRole } from '../api/userApi';

const toDateInputValue = (isoString) =>
  isoString ? isoString.slice(0, 10) : '';

const toLocalDateTime = (dateStr) =>
  dateStr ? `${dateStr}T00:00:00` : null;

const toNullableLong = (value) =>
  value ? Number(value) : null;

const userLabel = (u) =>
  `${u.firstName || ''} ${u.lastName || ''}`.trim();

const STATUS_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const ChantierEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const {
    fetchChantierById,
    editChantier,
    loading,
    error,
  } = useChantiers();

  const { user } = useAuth();

  const [form, setForm] = useState(null);
  const [accessDenied, setAccessDenied] = useState(false);

  const isAdmin = user?.role === 'ADMIN';
  const isChefProjet = user?.role === 'CHEF_PROJET';

  const [clients, setClients] = useState([]);
  const [chefsProjet, setChefsProjet] = useState([]);
  const [chefsChantier, setChefsChantier] = useState([]);
  const [magasiniers, setMagasiniers] = useState([]);
  const [agentsSaisie, setAgentsSaisie] = useState([]);

  const [loadingOptions, setLoadingOptions] = useState(true);
  const [optionsError, setOptionsError] = useState(null);

  /* =========================================================
     CHARGEMENT DU CHANTIER
     ========================================================= */

  useEffect(() => {
    fetchChantierById(id).then((data) => {
      const isOwner =
        isChefProjet &&
        data.chefProjet?.id != null &&
        user?.id != null &&
        Number(data.chefProjet.id) === Number(user.id);

      if (!isAdmin && !isOwner) {
        setAccessDenied(true);
        return;
      }

      setForm({
        name: data.name || '',
        reference: data.reference || '',
        address: data.address || '',
        description: data.description || '',
        latitude: data.latitude ?? null,
        longitude: data.longitude ?? null,
        status: data.status || 'PLANIFIE',
        startDate: toDateInputValue(data.startDate),
        endDate: toDateInputValue(data.endDate),
        clientId: data.client?.id || '',
        chefProjetId: data.chefProjet?.id || '',
        magasinierId: data.magasinier?.id || '',
        agentSaisieId: data.agentSaisie?.id || '',
        chefChantierId: data.chefChantier?.id || '',
      });
    });

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  /* =========================================================
     CHARGEMENT DES OPTIONS
     ========================================================= */

  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      setOptionsError(null);

      try {
        const requests = [
          getClients({ size: 200 }),
          getUsersByRole('CHEF_CHANTIER'),
          getUsersByRole('MAGASINIER'),
          getUsersByRole('AGENT_SAISIE'),
        ];

        if (isAdmin) {
          requests.push(getUsersByRole('CHEF_PROJET'));
        }

        const results = await Promise.all(requests);

        setClients(
          results[0]?.content ??
          results[0] ??
          []
        );

        setChefsChantier(results[1] ?? []);
        setMagasiniers(results[2] ?? []);
        setAgentsSaisie(results[3] ?? []);

        if (isAdmin) {
          setChefsProjet(results[4] ?? []);
        }
      } catch (err) {
        setOptionsError(
          'Impossible de charger les listes (clients/utilisateurs).'
        );
      } finally {
        setLoadingOptions(false);
      }
    };

    loadOptions();
  }, [isAdmin]);

  /* =========================================================
     MODIFICATION DES CHAMPS
     ========================================================= */

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleLocationChange = (lat, lng) => {
    setForm((prev) => ({
      ...prev,
      latitude: lat,
      longitude: lng,
    }));
  };

  /* =========================================================
     ENREGISTREMENT
     ========================================================= */

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      name: form.name,
      reference: form.reference || null,
      address: form.address || null,
      description: form.description || null,

      latitude: form.latitude,
      longitude: form.longitude,

      status: form.status,

      startDate: toLocalDateTime(form.startDate),
      endDate: toLocalDateTime(form.endDate),

      clientId: toNullableLong(form.clientId),

      chefProjetId: isChefProjet
        ? user.id
        : toNullableLong(form.chefProjetId),

      magasinierId:
        toNullableLong(form.magasinierId),

      agentSaisieId:
        toNullableLong(form.agentSaisieId),

      chefChantierId:
        toNullableLong(form.chefChantierId),
    };

    try {
      await editChantier(id, payload);

      navigate(`/chantiers/${id}`);
    } catch (err) {
      // L'erreur est déjà exposée via le hook
    }
  };

  /* =========================================================
     ACCÈS REFUSÉ
     ========================================================= */

  if (accessDenied) {
    return (
      <div className="chantiers-page edit-chantiers-page">

        <div className="edit-access-denied">

          <div className="edit-access-denied__icon">
            🔒
          </div>

          <h2>
            Accès refusé
          </h2>

          <p>
            Vous n'êtes pas autorisé à modifier ce chantier.
          </p>

          <button
            className="btn-view"
            onClick={() => navigate('/chantiers')}
          >
            ← Retour aux chantiers
          </button>

        </div>

      </div>
    );
  }

  /* =========================================================
     LOADING
     ========================================================= */

  if (!form) {
    return (
      <div className="loading edit-loading">
        <div className="edit-loading-spinner" />
        Chargement du chantier...
      </div>
    );
  }

  const selectedClient = clients.find(
    (client) =>
      Number(client.id) === Number(form.clientId)
  );

  const selectedChefProjet = chefsProjet.find(
    (chef) =>
      Number(chef.id) === Number(form.chefProjetId)
  );

  const selectedChefChantier = chefsChantier.find(
    (chef) =>
      Number(chef.id) === Number(form.chefChantierId)
  );

  const selectedMagasinier = magasiniers.find(
    (userItem) =>
      Number(userItem.id) === Number(form.magasinierId)
  );

  const selectedAgent = agentsSaisie.find(
    (userItem) =>
      Number(userItem.id) === Number(form.agentSaisieId)
  );

  return (
    <div className="chantiers-page edit-chantiers-page">

      {/* =====================================================
          EN-TÊTE
          ===================================================== */}

      <div className="edit-page-header">

        <div className="edit-page-header__left">

          <button
            type="button"
            className="edit-back-button"
            onClick={() =>
              navigate(`/chantiers/${id}`)
            }
          >
            ←
          </button>

          <div>

            <div className="edit-page-eyebrow">
              CHANTIER
            </div>

            <h1>
              Modifier le chantier
            </h1>

            <p>
              Modifiez les informations et l'organisation
              de ce chantier.
            </p>

          </div>

        </div>

        <div className="edit-header-reference">
          <span>
            Référence
          </span>

          <strong>
            {form.reference || 'Non définie'}
          </strong>
        </div>

      </div>

      {/* =====================================================
          ALERTES
          ===================================================== */}

      {error && (
        <div className="edit-alert edit-alert--error">
          <span className="edit-alert__icon">
            ✕
          </span>

          <div>
            <strong>
              Erreur lors de l'enregistrement
            </strong>

            <p>
              {error}
            </p>
          </div>
        </div>
      )}

      {optionsError && (
        <div className="edit-alert edit-alert--warning">
          <span className="edit-alert__icon">
            !
          </span>

          <div>
            <strong>
              Attention
            </strong>

            <p>
              {optionsError}
            </p>
          </div>
        </div>
      )}

      {/* =====================================================
          FORMULAIRE + APERÇU
          ===================================================== */}

      <div className="edit-layout">

        {/* ===================================================
            FORMULAIRE PRINCIPAL
            =================================================== */}

        <form
          onSubmit={handleSubmit}
          className="edit-form"
        >

          {/* =================================================
              INFORMATIONS GÉNÉRALES
              ================================================= */}

          <section className="edit-section">

            <div className="edit-section__header">

              <div className="edit-section__icon">
                🏗️
              </div>

              <div>
                <h2>
                  Informations générales
                </h2>

                <p>
                  Identité et description du chantier
                </p>
              </div>

            </div>

            <div className="edit-section__body">

              <div className="edit-form-grid edit-form-grid--2">

                <div className="edit-form-group edit-form-group--full">

                  <label htmlFor="name">
                    Nom du site
                    <span>*</span>
                  </label>

                  <input
                    id="name"
                    type="text"
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    placeholder="Ex. Construction immeuble..."
                    required
                  />

                </div>

                <div className="edit-form-group">

                  <label htmlFor="reference">
                    Référence
                  </label>

                  <input
                    id="reference"
                    type="text"
                    name="reference"
                    value={form.reference}
                    onChange={handleChange}
                    placeholder="Ex. CH-2026-001"
                  />

                </div>

                <div className="edit-form-group">

                  <label htmlFor="status">
                    Statut
                  </label>

                  <select
                    id="status"
                    name="status"
                    value={form.status}
                    onChange={handleChange}
                  >
                    <option value="PLANIFIE">
                      Planifié
                    </option>

                    <option value="EN_COURS">
                      En cours
                    </option>

                    <option value="TERMINE">
                      Terminé
                    </option>

                    <option value="SUSPENDU">
                      Suspendu
                    </option>
                  </select>

                </div>

              </div>

              <div className="edit-form-group">

                <label htmlFor="address">
                  Adresse
                </label>

                <input
                  id="address"
                  type="text"
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                  placeholder="Adresse complète du chantier"
                />

              </div>

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
                  placeholder="Décrivez le chantier, les contraintes d'accès, les particularités..."
                />

                <span className="edit-field-help">
                  Ajoutez les informations utiles à l'équipe
                  qui intervient sur le chantier.
                </span>

              </div>

            </div>

          </section>

          {/* =================================================
              LOCALISATION
              ================================================= */}

          <section className="edit-section">

            <div className="edit-section__header">

              <div className="edit-section__icon">
                📍
              </div>

              <div>
                <h2>
                  Localisation
                </h2>

                <p>
                  Position géographique du chantier
                </p>
              </div>

            </div>

            <div className="edit-section__body">

              <LocationPicker
                address={form.address}
                latitude={form.latitude}
                longitude={form.longitude}
                onChange={handleLocationChange}
              />

            </div>

          </section>

          {/* =================================================
              PLANNING
              ================================================= */}

          <section className="edit-section">

            <div className="edit-section__header">

              <div className="edit-section__icon">
                📅
              </div>

              <div>
                <h2>
                  Planning
                </h2>

                <p>
                  Dates prévisionnelles du chantier
                </p>
              </div>

            </div>

            <div className="edit-section__body">

              <div className="edit-form-grid edit-form-grid--2">

                <div className="edit-form-group">

                  <label htmlFor="startDate">
                    Date de début
                  </label>

                  <input
                    id="startDate"
                    type="date"
                    name="startDate"
                    value={form.startDate}
                    onChange={handleChange}
                  />

                </div>

                <div className="edit-form-group">

                  <label htmlFor="endDate">
                    Date de fin prévisionnelle
                  </label>

                  <input
                    id="endDate"
                    type="date"
                    name="endDate"
                    value={form.endDate}
                    onChange={handleChange}
                  />

                </div>

              </div>

            </div>

          </section>

          {/* =================================================
              ÉQUIPE
              ================================================= */}

          <section className="edit-section">

            <div className="edit-section__header">

              <div className="edit-section__icon">
                👥
              </div>

              <div>
                <h2>
                  Équipe et responsables
                </h2>

                <p>
                  Affectation des responsables du chantier
                </p>
              </div>

            </div>

            <div className="edit-section__body">

              {/* CLIENT + CHEF PROJET */}

              <div className="edit-form-grid edit-form-grid--2">

                <div className="edit-form-group">

                  <label htmlFor="clientId">
                    Client
                    <span>*</span>
                  </label>

                  <select
                    id="clientId"
                    name="clientId"
                    value={form.clientId}
                    onChange={handleChange}
                    required
                    disabled={loadingOptions}
                  >
                    <option value="">
                      {loadingOptions
                        ? 'Chargement...'
                        : '— Sélectionner un client —'}
                    </option>

                    {clients.map((client) => (
                      <option
                        key={client.id}
                        value={client.id}
                      >
                        {client.name}
                      </option>
                    ))}
                  </select>

                </div>

                <div className="edit-form-group">

                  <label htmlFor="chefProjetId">
                    Chef de projet
                    <span>*</span>
                  </label>

                  {isChefProjet ? (

                    <div className="edit-disabled-field">

                      <div className="edit-user-mini-avatar">
                        {(user?.firstName?.[0] || '') +
                          (user?.lastName?.[0] || '')}
                      </div>

                      <span>
                        {`${user?.firstName || ''} ${
                          user?.lastName || ''
                        }`.trim() || 'Vous'}
                      </span>

                      <span className="edit-you-badge">
                        Vous
                      </span>

                    </div>

                  ) : (

                    <select
                      id="chefProjetId"
                      name="chefProjetId"
                      value={form.chefProjetId}
                      onChange={handleChange}
                      required
                      disabled={loadingOptions}
                    >
                      <option value="">
                        {loadingOptions
                          ? 'Chargement...'
                          : '— Sélectionner —'}
                      </option>

                      {chefsProjet.map((u) => (
                        <option
                          key={u.id}
                          value={u.id}
                        >
                          {userLabel(u)}
                        </option>
                      ))}
                    </select>

                  )}

                </div>

              </div>

              {/* MAGASINIER + AGENT */}

              <div className="edit-form-grid edit-form-grid--2">

                <div className="edit-form-group">

                  <label htmlFor="magasinierId">
                    Magasinier
                  </label>

                  <select
                    id="magasinierId"
                    name="magasinierId"
                    value={form.magasinierId}
                    onChange={handleChange}
                    disabled={loadingOptions}
                  >
                    <option value="">
                      {loadingOptions
                        ? 'Chargement...'
                        : '— Aucun —'}
                    </option>

                    {magasiniers.map((u) => (
                      <option
                        key={u.id}
                        value={u.id}
                      >
                        {userLabel(u)}
                      </option>
                    ))}
                  </select>

                </div>

                <div className="edit-form-group">

                  <label htmlFor="agentSaisieId">
                    Agent de saisie
                  </label>

                  <select
                    id="agentSaisieId"
                    name="agentSaisieId"
                    value={form.agentSaisieId}
                    onChange={handleChange}
                    disabled={loadingOptions}
                  >
                    <option value="">
                      {loadingOptions
                        ? 'Chargement...'
                        : '— Aucun —'}
                    </option>

                    {agentsSaisie.map((u) => (
                      <option
                        key={u.id}
                        value={u.id}
                      >
                        {userLabel(u)}
                      </option>
                    ))}
                  </select>

                </div>

              </div>

              {/* CHEF CHANTIER */}

              <div className="edit-form-group">

                <label htmlFor="chefChantierId">
                  Chef de chantier
                </label>

                <select
                  id="chefChantierId"
                  name="chefChantierId"
                  value={form.chefChantierId}
                  onChange={handleChange}
                  disabled={loadingOptions}
                >
                  <option value="">
                    {loadingOptions
                      ? 'Chargement...'
                      : '— Aucun —'}
                  </option>

                  {chefsChantier.map((u) => (
                    <option
                      key={u.id}
                      value={u.id}
                    >
                      {userLabel(u)}
                    </option>
                  ))}
                </select>

              </div>

            </div>

          </section>

          {/* =================================================
              ACTIONS
              ================================================= */}

          <div className="edit-form-actions">

            <button
              type="button"
              className="edit-cancel-button"
              onClick={() =>
                navigate(`/chantiers/${id}`)
              }
            >
              Annuler
            </button>

            <button
              type="submit"
              className="edit-save-button"
              disabled={
                loading ||
                loadingOptions
              }
            >
              {loading ? (
                <>
                  <span className="edit-button-spinner" />
                  Enregistrement...
                </>
              ) : (
                <>
                  ✓ Enregistrer les modifications
                </>
              )}
            </button>

          </div>

        </form>

        {/* ===================================================
            SIDEBAR / APERÇU
            =================================================== */}

        <aside className="edit-sidebar">

          <div className="edit-preview-card">

            <div className="edit-preview-card__header">

              <div className="edit-preview-icon">
                👁️
              </div>

              <div>
                <span>
                  APERÇU
                </span>

                <h2>
                  Votre chantier
                </h2>
              </div>

            </div>

            {/* NOM */}

            <div className="edit-preview-title">

              <div className="edit-preview-title__icon">
                🏗️
              </div>

              <div>

                <h3>
                  {form.name ||
                    'Nom du chantier'}
                </h3>

                <span>
                  {form.reference ||
                    'Référence non définie'}
                </span>

              </div>

            </div>

            {/* STATUT */}

            <div className="edit-preview-status">

              <span className="edit-preview-label">
                Statut
              </span>

              <span
                className={`status-badge status-${(
                  form.status || ''
                ).toLowerCase()}`}
              >
                {STATUS_LABELS[form.status] ||
                  form.status}
              </span>

            </div>

            {/* INFOS */}

            <div className="edit-preview-info-list">

              <div className="edit-preview-info">

                <span className="edit-preview-info__icon">
                  📍
                </span>

                <div>
                  <span>
                    Adresse
                  </span>

                  <strong>
                    {form.address ||
                      'Aucune adresse renseignée'}
                  </strong>
                </div>

              </div>

              <div className="edit-preview-info">

                <span className="edit-preview-info__icon">
                  📅
                </span>

                <div>
                  <span>
                    Planning
                  </span>

                  <strong>
                    {form.startDate
                      ? form.startDate
                      : 'Date de début non définie'}
                  </strong>

                  {form.endDate && (
                    <small>
                      → {form.endDate}
                    </small>
                  )}
                </div>

              </div>

              <div className="edit-preview-info">

                <span className="edit-preview-info__icon">
                  🌐
                </span>

                <div>
                  <span>
                    Coordonnées
                  </span>

                  <strong>
                    {form.latitude != null &&
                    form.longitude != null
                      ? `${Number(form.latitude).toFixed(
                          5
                        )}, ${Number(form.longitude).toFixed(
                          5
                        )}`
                      : 'Position non définie'}
                  </strong>
                </div>

              </div>

            </div>

            {/* CLIENT */}

            <div className="edit-preview-team">

              <div className="edit-preview-section-title">
                Client
              </div>

              <div className="edit-preview-person">

                <div className="edit-preview-avatar">
                  🏢
                </div>

                <div>

                  <strong>
                    {selectedClient?.name ||
                      'Aucun client sélectionné'}
                  </strong>

                  <span>
                    Client du chantier
                  </span>

                </div>

              </div>

            </div>

            {/* ÉQUIPE */}

            <div className="edit-preview-team">

              <div className="edit-preview-section-title">
                Équipe
              </div>

              <div className="edit-preview-person">

                <div className="edit-preview-avatar">
                  👨‍💼
                </div>

                <div>

                  <strong>
                    {isChefProjet
                      ? `${user?.firstName || ''} ${
                          user?.lastName || ''
                        }`.trim() || 'Vous'
                      : selectedChefProjet
                        ? userLabel(selectedChefProjet)
                        : 'Non affecté'}
                  </strong>

                  <span>
                    Chef de projet
                  </span>

                </div>

              </div>

              <div className="edit-preview-person">

                <div className="edit-preview-avatar">
                  👷
                </div>

                <div>

                  <strong>
                    {selectedChefChantier
                      ? userLabel(
                          selectedChefChantier
                        )
                      : 'Non affecté'}
                  </strong>

                  <span>
                    Chef de chantier
                  </span>

                </div>

              </div>

              <div className="edit-preview-person">

                <div className="edit-preview-avatar">
                  📦
                </div>

                <div>

                  <strong>
                    {selectedMagasinier
                      ? userLabel(selectedMagasinier)
                      : 'Non affecté'}
                  </strong>

                  <span>
                    Magasinier
                  </span>

                </div>

              </div>

              <div className="edit-preview-person">

                <div className="edit-preview-avatar">
                  📝
                </div>

                <div>

                  <strong>
                    {selectedAgent
                      ? userLabel(selectedAgent)
                      : 'Non affecté'}
                  </strong>

                  <span>
                    Agent de saisie
                  </span>

                </div>

              </div>

            </div>

          </div>

          {/* CONSEIL */}

          <div className="edit-help-card">

            <div className="edit-help-icon">
              💡
            </div>

            <div>

              <strong>
                Conseil
              </strong>

              <p>
                Vérifiez les dates, le statut et les
                responsables avant d'enregistrer les
                modifications.
              </p>

            </div>

          </div>

        </aside>

      </div>

    </div>
  );
};

export default ChantierEditPage;