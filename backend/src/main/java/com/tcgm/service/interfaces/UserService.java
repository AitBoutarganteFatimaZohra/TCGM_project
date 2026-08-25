package com.tcgm.service;

import com.tcgm.dto.request.ChangePasswordRequest;
import com.tcgm.dto.request.UserCreateRequest;
import com.tcgm.dto.request.UserUpdateRequest;
import com.tcgm.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(Long id);
    void enableUser(Long id, Boolean enabled);
    UserResponse assignRole(Long userId, String roleName);
    void changePassword(String email, ChangePasswordRequest request);

    // ⚠️ NOUVEAU : liste légère des utilisateurs ayant un rôle donné —
    // utilisé pour peupler les <select> Chef de Chantier / Magasinier /
    // Agent de Saisie / Chef de Projet côté frontend.
    List<UserResponse> getUsersByRole(String roleName);
}