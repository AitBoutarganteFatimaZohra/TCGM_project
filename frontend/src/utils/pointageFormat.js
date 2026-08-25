// Utilitaires d'affichage partagés pour le module Pointage (§8 cahier des charges)

export const STATUT_LABELS = {
  EN_ATTENTE: 'En attente',
  EN_ATTENTE_VALIDATION: 'En attente de validation',
  VALIDE: 'Validé',
  REJETE: 'Rejeté',
};

export const STATUT_BADGE_CLASS = {
  EN_ATTENTE: 'badge badge--neutral',
  EN_ATTENTE_VALIDATION: 'badge badge--warning',
  VALIDE: 'badge badge--success',
  REJETE: 'badge badge--danger',
};

export const STATUTS = ['EN_ATTENTE', 'EN_ATTENTE_VALIDATION', 'VALIDE', 'REJETE'];

// "2026-08-20" -> "20/08/2026"
export const formatDateFr = (dateStr) => {
  if (!dateStr) return '—';
  const d = new Date(`${dateStr}T00:00:00`);
  if (isNaN(d)) return dateStr;
  return d.toLocaleDateString('fr-FR');
};

// "2026-08-20T08:00:00" -> "08:00"
export const formatHeure = (isoString) => {
  if (!isoString) return '';
  const match = String(isoString).match(/T(\d{2}:\d{2})/);
  if (match) return match[1];
  const d = new Date(isoString);
  if (isNaN(d)) return '';
  return d.toTimeString().slice(0, 5);
};

// -> "08:00 → 17:00"
export const formatHoraire = (start, end, halfDay) => {
  if (halfDay && (!start || !end)) return 'Demi-journée';
  if (!start || !end) return '—';
  return `${formatHeure(start)} → ${formatHeure(end)}`;
};

// 7.5 -> "7.5 h" / 9 -> "9 h"
export const formatTotalHeures = (heures) => {
  if (heures === null || heures === undefined) return '0 h';
  const isInteger = Number.isInteger(heures);
  return `${isInteger ? heures : heures.toFixed(1)} h`;
};

const extractMotifRejet = (notes) => {
  if (!notes) return null;
  const match = notes.match(/Motif du rejet\s*:\s*([\s\S]*)$/);
  return match ? match[1].trim() : null;
};

// Message dynamique de statut (§4 cahier des charges)
export const getStatusMessage = (dossier) => {
  switch (dossier?.status) {
    case 'EN_ATTENTE':
      return { type: 'info', text: 'Ce dossier est en cours de saisie. Vous pouvez le modifier ou le soumettre.' };
    case 'EN_ATTENTE_VALIDATION':
      return { type: 'info', text: 'Ce dossier a été soumis. En attente de validation par le Chef de Chantier.' };
    case 'VALIDE':
      return { type: 'success', text: '✅ Ce dossier a été validé par le Chef de Chantier.' };
    case 'REJETE': {
      const motif = extractMotifRejet(dossier?.notes);
      return {
        type: 'danger',
        text: `❌ Ce dossier a été rejeté par le Chef de Chantier.${motif ? ` Motif : ${motif}` : ''}`,
      };
    }
    default:
      return null;
  }
};

// Créneaux fixes de la demi-journée (§3 cahier des charges)
export const HALF_DAY_SLOTS = {
  MATIN: { start: '08:00', end: '12:00' },
  APRES_MIDI: { start: '13:00', end: '17:00' },
};

export const buildHalfDayTimes = (date, slot) => {
  const s = HALF_DAY_SLOTS[slot] || HALF_DAY_SLOTS.MATIN;
  return {
    startTime: `${date}T${s.start}`,
    endTime: `${date}T${s.end}`,
  };
};