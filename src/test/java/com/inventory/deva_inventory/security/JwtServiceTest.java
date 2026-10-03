package com.inventory.deva_inventory.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static JwtProperties props(String secret, Duration accessExpiration) {
        return new JwtProperties(secret, "deva-inventory", accessExpiration, Duration.ofHours(1));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService(props("secret", Duration.ofMinutes(5))));
    }

    @Test
    void generatesRandomSecretWhenMissing() {
        JwtService a = new JwtService(props("", Duration.ofMinutes(5)));
        JwtService b = new JwtService(props(null, Duration.ofMinutes(5)));
        String token = a.createAccessToken("alice", List.of("Admin"));
        assertEquals("alice", a.verifyAccessToken(token).getSubject());
        assertThrows(JWTVerificationException.class, () -> b.verifyAccessToken(token));
    }

    @Test
    void expiredAccessTokenIsRejected() {
        JwtService service = new JwtService(props("0123456789abcdef0123456789abcdef", Duration.ofSeconds(-1)));
        String token = service.createAccessToken("alice", List.of("Admin"));
        assertThrows(JWTVerificationException.class, () -> service.verifyAccessToken(token));
    }
}
