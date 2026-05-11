package com.piisw.tod.dto.auth;

public record AuthResponse(
        String token,
        Long userId,
        String email
) {
}
