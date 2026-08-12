package com.tcgm.config;

import com.tcgm.model.enums.RoleName;
import com.tcgm.model.User;
import com.tcgm.model.Role;
import com.tcgm.model.Permission;
import com.tcgm.repository.UserRepository;
import com.tcgm.repository.RoleRepository;
import com.tcgm.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialiser les données uniquement si la base est vide
        if (roleRepository.count() == 0) {
            log.info("Initialisation des données de base...");
            initializeRolesAndPermissions();
            createAdminUser();
            createResponsableRHUser();
            log.info("Initialisation terminée avec succès !");
        } else {
            log.info("Les rôles existent déjà, vérification de l'utilisateur admin...");
            // Vérifier si l'admin existe, sinon le créer
            if (userRepository.findByEmail("admin@tcgm.com").isEmpty()) {
                createAdminUser();
            }
            // Vérifier si le responsable RH existe, sinon le créer
            if (userRepository.findByEmail("rh@tcgm.com").isEmpty()) {
                createResponsableRHUser();
            }
        }
    }

    private void initializeRolesAndPermissions() {
        // Créer les permissions
        Permission userCreate = createPermission("USER_CREATE", "Créer des utilisateurs");
        Permission userRead = createPermission("USER_READ", "Lire les utilisateurs");
        Permission userUpdate = createPermission("USER_UPDATE", "Modifier les utilisateurs");
        Permission userDelete = createPermission("USER_DELETE", "Supprimer des utilisateurs");
        
        Permission siteCreate = createPermission("SITE_CREATE", "Créer des sites");
        Permission siteRead = createPermission("SITE_READ", "Lire les sites");
        Permission siteUpdate = createPermission("SITE_UPDATE", "Modifier les sites");
        Permission siteDelete = createPermission("SITE_DELETE", "Supprimer des sites");
        
        Permission clientCreate = createPermission("CLIENT_CREATE", "Créer des clients");
        Permission clientRead = createPermission("CLIENT_READ", "Lire les clients");
        Permission clientUpdate = createPermission("CLIENT_UPDATE", "Modifier les clients");
        Permission clientDelete = createPermission("CLIENT_DELETE", "Supprimer des clients");
        
        Permission ouvrierCreate = createPermission("OUVRIER_CREATE", "Créer des ouvriers");
        Permission ouvrierRead = createPermission("OUVRIER_READ", "Lire les ouvriers");
        Permission ouvrierUpdate = createPermission("OUVRIER_UPDATE", "Modifier les ouvriers");
        Permission ouvrierDelete = createPermission("OUVRIER_DELETE", "Supprimer des ouvriers");
        
        Permission tacheCreate = createPermission("TACHE_CREATE", "Créer des tâches");
        Permission tacheRead = createPermission("TACHE_READ", "Lire les tâches");
        Permission tacheUpdate = createPermission("TACHE_UPDATE", "Modifier les tâches");
        Permission tacheDelete = createPermission("TACHE_DELETE", "Supprimer des tâches");
        
        Permission pointageCreate = createPermission("POINTAGE_CREATE", "Créer des pointages");
        Permission pointageRead = createPermission("POINTAGE_READ", "Lire les pointages");
        Permission pointageUpdate = createPermission("POINTAGE_UPDATE", "Modifier les pointages");
        Permission pointageDelete = createPermission("POINTAGE_DELETE", "Supprimer des pointages");
        Permission pointageValidate = createPermission("POINTAGE_VALIDATE", "Valider les pointages");
        
        Permission journalRead = createPermission("JOURNAL_READ", "Lire le journal");
        Permission journalExport = createPermission("JOURNAL_EXPORT", "Exporter le journal");
        Permission journalValidate = createPermission("JOURNAL_VALIDATE", "Valider les entrées du journal");

        // Créer les rôles avec leurs permissions
        Set<Permission> allPermissions = new HashSet<>();
        allPermissions.add(userCreate); allPermissions.add(userRead); 
        allPermissions.add(userUpdate); allPermissions.add(userDelete);
        allPermissions.add(siteCreate); allPermissions.add(siteRead);
        allPermissions.add(siteUpdate); allPermissions.add(siteDelete);
        allPermissions.add(clientCreate); allPermissions.add(clientRead);
        allPermissions.add(clientUpdate); allPermissions.add(clientDelete);
        allPermissions.add(ouvrierCreate); allPermissions.add(ouvrierRead);
        allPermissions.add(ouvrierUpdate); allPermissions.add(ouvrierDelete);
        allPermissions.add(tacheCreate); allPermissions.add(tacheRead);
        allPermissions.add(tacheUpdate); allPermissions.add(tacheDelete);
        allPermissions.add(pointageCreate); allPermissions.add(pointageRead);
        allPermissions.add(pointageUpdate); allPermissions.add(pointageDelete);
        allPermissions.add(pointageValidate);
        allPermissions.add(journalRead); allPermissions.add(journalExport);
        allPermissions.add(journalValidate);

        // =========================================================
        // RÔLE ADMIN - toutes les permissions
        // =========================================================
        Role adminRole = Role.builder()
            .name(RoleName.ADMIN)
            .description("Administrateur système - Accès complet")
            .permissions(allPermissions)
            .build();

        // =========================================================
        // RÔLE CHEF_PROJET
        // =========================================================
        Set<Permission> chefProjetPermissions = new HashSet<>();
        chefProjetPermissions.add(siteCreate); chefProjetPermissions.add(siteRead);
        chefProjetPermissions.add(siteUpdate);
        chefProjetPermissions.add(clientRead);
        chefProjetPermissions.add(ouvrierRead);
        chefProjetPermissions.add(tacheRead);
        chefProjetPermissions.add(pointageRead);
        chefProjetPermissions.add(journalRead);

        Role chefProjetRole = Role.builder()
            .name(RoleName.CHEF_PROJET)
            .description("Chef de projet - Supervise les chantiers")
            .permissions(chefProjetPermissions)
            .build();

        // =========================================================
        // RÔLE CHEF_CHANTIER
        // =========================================================
        Set<Permission> chefChantierPermissions = new HashSet<>();
        chefChantierPermissions.add(siteRead);
        chefChantierPermissions.add(ouvrierRead); chefChantierPermissions.add(ouvrierCreate);
        chefChantierPermissions.add(ouvrierUpdate);
        chefChantierPermissions.add(tacheCreate); chefChantierPermissions.add(tacheRead);
        chefChantierPermissions.add(tacheUpdate);
        chefChantierPermissions.add(pointageRead);
        chefChantierPermissions.add(pointageValidate);

        Role chefChantierRole = Role.builder()
            .name(RoleName.CHEF_CHANTIER)
            .description("Chef de chantier - Gère les ouvriers et les tâches")
            .permissions(chefChantierPermissions)
            .build();

        // =========================================================
        // RÔLE MAGASINIER
        // =========================================================
        Set<Permission> magasinierPermissions = new HashSet<>();
        magasinierPermissions.add(siteRead);
        magasinierPermissions.add(ouvrierRead);
        magasinierPermissions.add(tacheRead);

        Role magasinierRole = Role.builder()
            .name(RoleName.MAGASINIER)
            .description("Magasinier - Gère les ressources")
            .permissions(magasinierPermissions)
            .build();

        // =========================================================
        // RÔLE AGENT_SAISIE
        // =========================================================
        Set<Permission> agentSaisiePermissions = new HashSet<>();
        agentSaisiePermissions.add(siteRead);
        agentSaisiePermissions.add(ouvrierRead);
        agentSaisiePermissions.add(tacheRead);
        agentSaisiePermissions.add(pointageCreate); agentSaisiePermissions.add(pointageRead);
        agentSaisiePermissions.add(pointageUpdate);

        Role agentSaisieRole = Role.builder()
            .name(RoleName.AGENT_SAISIE)
            .description("Agent de saisie - Saisie le pointage")
            .permissions(agentSaisiePermissions)
            .build();

        // =========================================================
        // NOUVEAU : RÔLE RESPONSABLE_RH
        // =========================================================
        Set<Permission> rhPermissions = new HashSet<>();
        // Permissions de lecture
        rhPermissions.add(userRead);
        rhPermissions.add(ouvrierRead);
        rhPermissions.add(pointageRead);
        rhPermissions.add(journalRead);
        // Permissions d'écriture sur les ouvriers
        rhPermissions.add(ouvrierCreate);
        rhPermissions.add(ouvrierUpdate);
        rhPermissions.add(ouvrierDelete);
        // Permissions sur les utilisateurs (pour gérer les comptes)
        rhPermissions.add(userCreate);
        rhPermissions.add(userUpdate);
        // Permissions sur les pointages (consultation uniquement)
        rhPermissions.add(pointageRead);

        Role rhRole = Role.builder()
            .name(RoleName.RESPONSABLE_RH)
            .description("Responsable RH - Gère les ouvriers et consulte les données RH")
            .permissions(rhPermissions)
            .build();

        // =========================================================
        // SAUVEGARDE DES RÔLES
        // =========================================================
        roleRepository.save(adminRole);
        roleRepository.save(chefProjetRole);
        roleRepository.save(chefChantierRole);
        roleRepository.save(magasinierRole);
        roleRepository.save(agentSaisieRole);
        roleRepository.save(rhRole);  // ← AJOUT

        log.info("Rôles et permissions initialisés avec succès");
    }

    private Permission createPermission(String name, String description) {
        return permissionRepository.save(
            Permission.builder()
                .name(name)
                .description(description)
                .build()
        );
    }

    // =========================================================
    // CRÉATION DE L'UTILISATEUR ADMIN
    // =========================================================
    private void createAdminUser() {
        // Vérifier si l'admin existe déjà
        if (userRepository.findByEmail("admin@tcgm.com").isPresent()) {
            log.info("L'utilisateur ADMIN existe déjà");
            return;
        }

        // Récupérer le rôle ADMIN
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé"));

        // Créer un Set avec le rôle admin
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        // Créer l'utilisateur admin avec le rôle inclus dans le builder
        User admin = User.builder()
            .email("admin@tcgm.com")
            .password(passwordEncoder.encode("admin123"))
            .firstName("Admin")
            .lastName("TCGM")
            .phone("+212 6 00 00 00 00")
            .enabled(true)
            .roles(roles)
            .build();

        userRepository.save(admin);
        log.info("Utilisateur ADMIN créé : admin@tcgm.com / admin123");
    }

    // =========================================================
    // NOUVEAU : CRÉATION DU RESPONSABLE RH
    // =========================================================
    private void createResponsableRHUser() {
        // Vérifier si le responsable RH existe déjà
        if (userRepository.findByEmail("rh@tcgm.com").isPresent()) {
            log.info("L'utilisateur RESPONSABLE_RH existe déjà");
            return;
        }

        // Récupérer le rôle RESPONSABLE_RH
        Role rhRole = roleRepository.findByName(RoleName.RESPONSABLE_RH)
            .orElseThrow(() -> new RuntimeException("Rôle RESPONSABLE_RH non trouvé"));

        // Créer un Set avec le rôle
        Set<Role> roles = new HashSet<>();
        roles.add(rhRole);

        // Créer l'utilisateur RH
        User rhUser = User.builder()
            .email("rh@tcgm.com")
            .password(passwordEncoder.encode("rh123"))
            .firstName("Responsable")
            .lastName("RH")
            .phone("+212 6 11 11 11 11")
            .enabled(true)
            .roles(roles)
            .build();

        userRepository.save(rhUser);
        log.info("Utilisateur RESPONSABLE_RH créé : rh@tcgm.com / rh123");
    }
}