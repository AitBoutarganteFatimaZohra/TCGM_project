package com.tcgm.security;

import com.tcgm.model.User;
import com.tcgm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur par email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("Utilisateur non trouvé avec l'email: {}", email);
                return new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
            });

        if (!user.getEnabled()) {
            log.warn("Tentative de connexion d'un utilisateur désactivé: {}", email);
            throw new UsernameNotFoundException("Ce compte utilisateur est désactivé");
        }

        log.debug("Utilisateur chargé avec succès: {}", email);
        return UserPrincipal.create(user);
    }

    /**
     * Charge un utilisateur par son ID (pour les opérations internes)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Chargement de l'utilisateur par ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Utilisateur non trouvé avec l'ID: {}", id);
                return new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
            });

        return UserPrincipal.create(user);
    }

    /**
     * Vérifie si un utilisateur existe par email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Récupère un utilisateur par email (sans les autorités)
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }
}