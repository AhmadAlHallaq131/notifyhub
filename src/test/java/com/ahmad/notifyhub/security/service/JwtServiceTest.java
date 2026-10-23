package com.ahmad.notifyhub.security.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void generatesTokenThatContainsTheUserEmail() {
        String secret = Base64.getEncoder().encodeToString(
                "test-secret-that-is-longer-than-thirty-two-bytes".getBytes(StandardCharsets.UTF_8)
        );
        JwtService jwtService = new JwtService(secret, 3_600_000L);

        String token = jwtService.generateToken("user@example.com");

        assertEquals("user@example.com", jwtService.extractEmail(token));
        assertEquals(3_600L, jwtService.getExpirationSeconds());
    }
}
