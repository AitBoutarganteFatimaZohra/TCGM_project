package com.tcgm.service;

import com.tcgm.dto.request.UserCreateRequest;
import com.tcgm.dto.request.UserUpdateRequest;
import com.tcgm.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(Long id);
    void enableUser(Long id, Boolean enabled);
    UserResponse assignRole(Long userId, String roleName);
}