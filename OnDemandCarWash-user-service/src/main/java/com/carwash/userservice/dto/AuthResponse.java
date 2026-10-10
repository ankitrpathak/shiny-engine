package com.carwash.userservice.dto;

import com.carwash.userservice.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
}
