package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.RegisterRequest;
import com.ahmad.notifyhub.dto.response.UserResponse;
import com.ahmad.notifyhub.entity.User;
import com.ahmad.notifyhub.exception.EmailAlreadyExistsException;
import com.ahmad.notifyhub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
