package com.tcgm.service.impl;

import com.tcgm.dto.request.UserCreateRequest;
import com.tcgm.dto.request.UserUpdateRequest;
import com.tcgm.dto.response.UserResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.UserMapper;
import com.tcgm.model.User;
import com.tcgm.model.Role;
import com.tcgm.model.enums.RoleName;
import com.tcgm.repository.UserRepository;
import com.tcgm.repository.RoleRepository;
import com.tcgm.service.UserService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JournalService journalService;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getEmail());

        // Vérifier si l'email existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Cet email est déjà utilisé");
        }

        // Récupérer le rôle
        String roleName = request.getRole() != null ? request.getRole() : "CHEF_PROJET";
        Role role = roleRepository.findByName(RoleName.valueOf(roleName))
            .orElseThrow(() -> new BadRequestException("Rôle invalide"));

        // Créer l'utilisateur
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .enabled(request.getEnabled() != null ? request.getEnabled() : true)
            .build();
        user.getRoles().add(role);

        user = userRepository.save(user);

        // Journaliser l'action
        journalService.logAction(
            TypeAction.CREATION,
            "USER",
            user.getId(),
            "Création de l'utilisateur: " + user.getEmail(),
            null
        );

        log.info("Utilisateur créé avec succès: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        user = userRepository.save(user);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "USER",
            user.getId(),
            "Mise à jour de l'utilisateur: " + user.getEmail(),
            null
        );

        log.info("Utilisateur mis à jour avec succès: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Récupération de l'utilisateur ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Récupération de l'utilisateur par email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Récupération de tous les utilisateurs");
        return userRepository.findAll(pageable)
            .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "USER",
            user.getId(),
            "Suppression de l'utilisateur: " + user.getEmail(),
            null
        );

        userRepository.delete(user);
        log.info("Utilisateur supprimé avec succès: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void enableUser(Long id, Boolean enabled) {
        log.info("{} de l'utilisateur ID: {}", enabled ? "Activation" : "Désactivation", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        user.setEnabled(enabled);
        userRepository.save(user);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "USER",
            user.getId(),
            (enabled ? "Activation" : "Désactivation") + " de l'utilisateur: " + user.getEmail(),
            null
        );

        log.info("Utilisateur {} avec succès: {}", enabled ? "activé" : "désactivé", user.getEmail());
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long userId, String roleName) {
        log.info("Assignation du rôle {} à l'utilisateur ID: {}", roleName, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        Role role = roleRepository.findByName(RoleName.valueOf(roleName))
            .orElseThrow(() -> new BadRequestException("Rôle invalide"));

        user.getRoles().clear();
        user.getRoles().add(role);
        user = userRepository.save(user);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "USER",
            user.getId(),
            "Assignation du rôle " + roleName + " à l'utilisateur: " + user.getEmail(),
            null
        );

        log.info("Rôle assigné avec succès à l'utilisateur: {}", user.getEmail());
        return userMapper.toResponse(user);
    }
}