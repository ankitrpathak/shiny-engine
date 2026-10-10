package com.carwash.userservice.service;

import com.carwash.userservice.dto.*;
import com.carwash.userservice.entity.Role;
import com.carwash.userservice.entity.User;
import com.carwash.userservice.exception.BadRequestException;
import com.carwash.userservice.repository.UserRepository;
import com.carwash.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserServiceImpl userService;

    private User customerUser;
    private User godUser;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .id(1L)
                .fullName("Test Customer")
                .email("test@example.com")
                .password("encoded_pass")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        godUser = User.builder()
                .id(99L)
                .fullName("God Admin")
                .email("god@carwash.com")
                .password("encoded_god")
                .role(Role.GOD)
                .active(true)
                .build();
    }

    @Test
    void registerUser_Success_AssignsCustomerRoleByDefault() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("New Customer")
                .email("new@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(tokenProvider.generateToken(2L, "new@example.com", "CUSTOMER")).thenReturn("dummy-jwt-token");

        AuthResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("new@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());
        assertEquals("dummy-jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Duplicate")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_ValidCredentials_ReturnsAuthResponse() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customerUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(tokenProvider.generateToken(1L, "test@example.com", "CUSTOMER")).thenReturn("jwt-login-token");

        AuthResponse response = userService.loginUser(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("jwt-login-token", response.getToken());
    }

    @Test
    void blockUser_GodAccount_ThrowsBadRequestException() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(godUser));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> userService.blockUser(99L));
        assertTrue(ex.getMessage().contains("God super administrator account cannot be blocked"));
        verify(userRepository, never()).save(godUser);
    }

    @Test
    void deleteUser_GodAccount_ThrowsBadRequestException() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(godUser));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> userService.deleteUser(99L, "god@carwash.com"));
        assertTrue(ex.getMessage().contains("God super administrator account cannot be deleted"));
        verify(userRepository, never()).delete(godUser);
    }

    @Test
    void demoteToCustomer_GodAccount_ThrowsBadRequestException() {
        when(userRepository.findByEmail("god@carwash.com")).thenReturn(Optional.of(godUser));
        when(userRepository.findById(99L)).thenReturn(Optional.of(godUser));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> userService.demoteToCustomer(99L, "god@carwash.com"));
        assertTrue(ex.getMessage().contains("God super administrator cannot be demoted"));
    }

    @Test
    void promoteToAdmin_Success_UpdatesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepository.save(any(User.class))).thenReturn(customerUser);

        UserResponse response = userService.promoteToAdmin(1L);

        assertEquals(Role.ADMIN, response.getRole());
        verify(userRepository).save(customerUser);
    }

    @Test
    void getUserSummary_CalculatesCountsCorrectly() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByActive(true)).thenReturn(8L);
        when(userRepository.countByActive(false)).thenReturn(2L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(6L);
        when(userRepository.countByRole(Role.WASHER)).thenReturn(2L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        when(userRepository.countByRole(Role.GOD)).thenReturn(1L);

        UserSummaryResponse summary = userService.getUserSummary();

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalUsers());
        assertEquals(8L, summary.getActiveUsers());
        assertEquals(2L, summary.getBlockedUsers());
        assertEquals(6L, summary.getCustomers());
        assertEquals(2L, summary.getWashers());
        assertEquals(2L, summary.getAdmins());
    }
}
