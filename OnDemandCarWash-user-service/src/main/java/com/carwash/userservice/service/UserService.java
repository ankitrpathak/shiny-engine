package com.carwash.userservice.service;

import com.carwash.userservice.dto.*;

import java.util.List;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);
    AuthResponse loginUser(LoginRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse getCurrentUser(String email);
    List<UserResponse> getAllUsers();
    List<UserResponse> getActiveWashers();
    UserResponse updateUser(Long id, UpdateProfileRequest request, String currentUserEmail);
    void deleteUser(Long id, String currentUserEmail);
    UserResponse blockUser(Long id);
    UserResponse activateUser(Long id);
    UserResponse promoteToAdmin(Long id);
    UserResponse promoteToWasher(Long id);
    UserResponse demoteToCustomer(Long id, String currentUserEmail);
    long countTotal();
    long countActive();
    long countBlocked();
    UserSummaryResponse getUserSummary();
}
