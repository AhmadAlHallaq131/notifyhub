package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.LoginRequest;
import com.ahmad.notifyhub.dto.request.RegisterRequest;
import com.ahmad.notifyhub.dto.response.LoginResponse;
import com.ahmad.notifyhub.dto.response.UserResponse;
import com.ahmad.notifyhub.entity.User;
import com.ahmad.notifyhub.exception.EmailAlreadyExistsException;
import com.ahmad.notifyhub.exception.InvalidCredentialsException;
import com.ahmad.notifyhub.repository.UserRepository;
import com.ahmad.notifyhub.security.service.JwtService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request){
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)){
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        String passwordHashed = passwordEncoder.encode(request.password());

        User user = new User(
                normalizedEmail,
                passwordHashed,
                LocalDateTime.now()
        );
        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }


    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request){
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException());

        boolean passwordMatch = passwordEncoder.matches(request.password(), user.getPasswordHash());

        if(!passwordMatch){
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(normalizedEmail);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
                );
    }

}
