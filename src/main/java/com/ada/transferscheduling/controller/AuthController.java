package com.ada.transferscheduling.controller;

import com.ada.transferscheduling.dto.request.LoginRequest;
import com.ada.transferscheduling.dto.response.LoginResponse;
import com.ada.transferscheduling.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and JWT token issuance")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Authenticates the user and returns a JWT token",
            description = "Test users: admin/admin123 (ROLE_ADMIN, ROLE_USER) and user/user123 (ROLE_USER)")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .build();

        return ResponseEntity.ok(response);
    }
}
