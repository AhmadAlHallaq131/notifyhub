package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.LoginRequest;
import com.ahmad.notifyhub.dto.request.RegisterRequest;
import com.ahmad.notifyhub.dto.response.LoginResponse;
import com.ahmad.notifyhub.dto.response.UserResponse;
import com.ahmad.notifyhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, authenticate, and retrieve the current user.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    @SecurityRequirements
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
            ){
        UserResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT access token")
    @SecurityRequirements
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
            ){
        LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ){
        UserResponse response = authService.getCurrentUser(
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
}
