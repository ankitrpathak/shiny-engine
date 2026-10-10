package com.carwash.userservice.service;

import com.carwash.userservice.dto.*;
import com.carwash.userservice.entity.Role;
import com.carwash.userservice.entity.User;
import com.carwash.userservice.exception.BadRequestException;
import com.carwash.userservice.exception.ResourceNotFoundException;
import com.carwash.userservice.exception.UnauthorizedException;
import com.carwash.userservice.repository.UserRepository;
import com.carwash.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final String GOD_EMAIL = "god@carwash.com";

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .active(savedUser.isActive())
                .build();
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Processing user login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account has been blocked or deactivated.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        return getUserByEmail(email);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getActiveWashers() {
        return userRepository.findByRoleAndActive(Role.WASHER, true).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateProfileRequest request, String currentUserEmail) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        boolean isSelf = targetUser.getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.GOD;

        if (!isSelf && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to update another user's profile.");
        }

        if (isGodUser(targetUser) && !currentUser.getEmail().equalsIgnoreCase(GOD_EMAIL)) {
            throw new UnauthorizedException("The God super administrator account cannot be edited by other users.");
        }

        if (StringUtils.hasText(request.getFullName())) {
            targetUser.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            targetUser.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            targetUser.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getPassword())) {
            targetUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(targetUser);
        log.info("User id {} profile updated successfully", id);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String currentUserEmail) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isGodUser(targetUser)) {
            throw new BadRequestException("The God super administrator account cannot be deleted.");
        }

        userRepository.delete(targetUser);
        log.info("User id {} deleted by {}", id, currentUserEmail);
    }

    @Override
    @Transactional
    public UserResponse blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isGodUser(user)) {
            throw new BadRequestException("The God super administrator account cannot be blocked.");
        }

        user.setActive(false);
        User updated = userRepository.save(user);
        log.info("User id {} blocked successfully", id);
        return mapToUserResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(true);
        User updated = userRepository.save(user);
        log.info("User id {} activated successfully", id);
        return mapToUserResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse promoteToAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isGodUser(user)) {
            throw new BadRequestException("The God super administrator role cannot be altered.");
        }

        user.setRole(Role.ADMIN);
        User updated = userRepository.save(user);
        log.info("User id {} promoted to ADMIN", id);
        return mapToUserResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse promoteToWasher(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isGodUser(user)) {
            throw new BadRequestException("The God super administrator role cannot be altered.");
        }

        user.setRole(Role.WASHER);
        User updated = userRepository.save(user);
        log.info("User id {} promoted to WASHER", id);
        return mapToUserResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse demoteToCustomer(Long id, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (currentUser.getRole() != Role.GOD) {
            throw new UnauthorizedException("Only the God super administrator can demote user roles.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isGodUser(user)) {
            throw new BadRequestException("The God super administrator cannot be demoted.");
        }

        user.setRole(Role.CUSTOMER);
        User updated = userRepository.save(user);
        log.info("User id {} demoted to CUSTOMER by God super admin", id);
        return mapToUserResponse(updated);
    }

    @Override
    public long countTotal() {
        return userRepository.count();
    }

    @Override
    public long countActive() {
        return userRepository.countByActive(true);
    }

    @Override
    public long countBlocked() {
        return userRepository.countByActive(false);
    }

    @Override
    public UserSummaryResponse getUserSummary() {
        return UserSummaryResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByActive(true))
                .blockedUsers(userRepository.countByActive(false))
                .customers(userRepository.countByRole(Role.CUSTOMER))
                .washers(userRepository.countByRole(Role.WASHER))
                .admins(userRepository.countByRole(Role.ADMIN) + userRepository.countByRole(Role.GOD))
                .build();
    }

    private boolean isGodUser(User user) {
        return user.getRole() == Role.GOD || GOD_EMAIL.equalsIgnoreCase(user.getEmail());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
