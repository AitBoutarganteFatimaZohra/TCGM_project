package com.tcgm.service.impl;

import com.tcgm.config.JwtTokenProvider;
import com.tcgm.dto.request.LoginRequest;
import com.tcgm.dto.request.RegisterRequest;
import com.tcgm.dto.response.AuthResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.UnauthorizedException;
import com.tcgm.model.User;
import com.tcgm.model.Role;
import com.tcgm.model.enums.RoleName;
import com.tcgm.repository.UserRepository;
import com.tcgm.repository.RoleRepository;
import com.tcgm.service.AuthService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JournalService journalService;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

        // Mettre à jour la date de dernière connexion
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Générer les tokens
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // Journaliser l'action
        journalService.logAction(
            TypeAction.CONNEXION,
            "USER",
            user.getId(),
            "Connexion de l'utilisateur: " + user.getEmail(),
            null
        );

        String roleName = user.getRoles().isEmpty() ? null : 
            user.getRoles().iterator().next().getName().name();

        log.info("Connexion réussie pour l'email: {}", request.getEmail());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(roleName)
            .expiresIn(tokenProvider.getAccessTokenExpiration())
            .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Cet email est déjà utilisé");
        }

        // Récupérer le rôle (par défaut: CHEF_PROJET)
        String roleName = request.getRole() != null ? request.getRole() : "CHEF_PROJET";
        Role role = roleRepository.findByName(RoleName.valueOf(roleName))
            .orElseThrow(() -> new BadRequestException("Rôle invalide: " + roleName));

        // Créer l'utilisateur
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .enabled(true)
            .build();
        user.getRoles().add(role);

        user = userRepository.save(user);

        // Générer les tokens
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // Journaliser l'action
        journalService.logAction(
            TypeAction.CREATION,
            "USER",
            user.getId(),
            "Inscription de l'utilisateur: " + user.getEmail(),
            null
        );

        log.info("Inscription réussie pour l'email: {}", request.getEmail());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(role.getName().name())
            .expiresIn(tokenProvider.getAccessTokenExpiration())
            .build();
    }

    @Override
    public void logout(String token) {
        // Journaliser l'action de déconnexion
        journalService.logAction(
            TypeAction.DECONNEXION,
            "USER",
            null,
            "Déconnexion",
            null
        );
        log.info("Déconnexion réussie");
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Tentative de rafraîchissement du token");

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Token de rafraîchissement invalide");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            email, null, null
        );

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        String roleName = user.getRoles().isEmpty() ? null : 
            user.getRoles().iterator().next().getName().name();

        log.info("Token rafraîchi avec succès pour l'email: {}", email);

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(roleName)
            .expiresIn(tokenProvider.getAccessTokenExpiration())
            .build();
    }
}