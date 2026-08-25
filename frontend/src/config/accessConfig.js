export const ROLES = {
  ADMIN: 'ADMIN',
  CHEF_PROJET: 'CHEF_PROJET',
  CHEF_CHANTIER: 'CHEF_CHANTIER',
  MAGASINIER: 'MAGASINIER',
  AGENT_SAISIE: 'AGENT_SAISIE',
};

export const ROUTE_ACCESS = {
  '/dashboard':            [ROLES.ADMIN, ROLES.CHEF_PROJET, ROLES.CHEF_CHANTIER, ROLES.MAGASINIER, ROLES.AGENT_SAISIE],
  '/chantiers':            [ROLES.ADMIN, ROLES.CHEF_PROJET],
  '/clients':              [ROLES.ADMIN, ROLES.CHEF_PROJET],
  '/ouvriers':             [ROLES.ADMIN, ROLES.CHEF_CHANTIER],
  '/taches':               [ROLES.ADMIN, ROLES.CHEF_CHANTIER, ROLES.CHEF_PROJET],
  '/travaux':              [ROLES.ADMIN, ROLES.CHEF_CHANTIER],
  '/affectations':         [ROLES.ADMIN, ROLES.CHEF_CHANTIER],
  '/pointage':             [ROLES.ADMIN, ROLES.CHEF_CHANTIER, ROLES.AGENT_SAISIE],
  '/ressources':           [ROLES.ADMIN, ROLES.MAGASINIER, ROLES.CHEF_CHANTIER, ROLES.CHEF_PROJET],
  '/mes-taches':           [ROLES.MAGASINIER],
  '/mon-journal':          [ROLES.MAGASINIER],
  '/mon-journal-agent':    [ROLES.AGENT_SAISIE],
  '/mon-profil':           [ROLES.ADMIN, ROLES.CHEF_PROJET, ROLES.CHEF_CHANTIER, ROLES.MAGASINIER, ROLES.AGENT_SAISIE],
  // 🔧 CORRIGÉ : CHEF_CHANTIER ajouté — il doit voir le journal des
  // rôles sous sa supervision (Magasinier, Agent de Saisie) pour
  // confirmer leurs actions en attente.
  '/journal':              [ROLES.ADMIN, ROLES.CHEF_PROJET, ROLES.CHEF_CHANTIER],
  '/statistiques':         [ROLES.ADMIN, ROLES.CHEF_PROJET],
  '/utilisateurs/nouveau': [ROLES.ADMIN],
};

export const hasAccess = (role, path) => {
  const basePath = '/' + path.split('/')[1];
  const allowed = ROUTE_ACCESS[basePath];
  return allowed ? allowed.includes(role) : true;
};