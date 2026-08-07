package com.tcgm.config;

import com.tcgm.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")  // ← SUPPRIMER dateTimeProviderRef
public class AuditConfig {

    /**
     * Fournit l'utilisateur actuel pour l'audit JPA
     * Utilisé pour les annotations @CreatedBy et @LastModifiedBy
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Si pas d'authentification, retourner SYSTEM
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }
            
            // Récupérer l'email de l'utilisateur authentifié
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                return Optional.of(userDetails.getUsername());
            }
            
            // Si le principal est une chaîne (cas d'erreur)
            if (principal instanceof String) {
                return Optional.of((String) principal);
            }
            
            // Si l'utilisateur est un UserPrincipal personnalisé
            if (principal instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) principal;
                return Optional.of(userPrincipal.getUsername());
            }
            
            return Optional.of("SYSTEM");
        };
    }

    // =========================================================
    // SUPPRIMEZ la méthode dateTimeProvider()
    // Spring Data JPA utilise LocalDateTime.now() par défaut
    // =========================================================
}