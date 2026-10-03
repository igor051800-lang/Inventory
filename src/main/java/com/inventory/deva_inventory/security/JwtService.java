package com.inventory.deva_inventory.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Single place where JWTs are issued and verified.
 */
@Component
public class JwtService {

    public static final String ROLES_CLAIM = "roles";
    static final String TOKEN_TYPE_CLAIM = "token_type";
    static final String ACCESS_TOKEN = "access";
    static final String REFRESH_TOKEN = "refresh";
    static final int MIN_SECRET_BYTES = 32;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(resolveSecret(properties.secret()));
        this.verifier = JWT.require(algorithm).withIssuer(properties.issuer()).build();
    }

    private static byte[] resolveSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("jwt.secret (JWT_SECRET) is not set; using a random per-process secret. "
                    + "Tokens will not survive a restart or work across instances. Set JWT_SECRET in production.");
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            return random;
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_BYTES + " bytes long for HMAC-SHA256");
        }
        return bytes;
    }

    public String createAccessToken(String userName, List<String> roles) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(userName)
                .withIssuer(properties.issuer())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(properties.accessTokenExpiration()))
                .withClaim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN)
                .withClaim(ROLES_CLAIM, roles)
                .sign(algorithm);
    }

    public String createRefreshToken(String userName) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(userName)
                .withIssuer(properties.issuer())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(properties.refreshTokenExpiration()))
                .withClaim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN)
                .sign(algorithm);
    }

    /**
     * Verifies signature, issuer and expiry, and that the token is an access token carrying a roles claim.
     */
    public DecodedJWT verifyAccessToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifier.verify(token);
        if (!ACCESS_TOKEN.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString())) {
            throw new JWTVerificationException("Not an access token");
        }
        if (jwt.getSubject() == null || jwt.getClaim(ROLES_CLAIM).asList(String.class) == null) {
            throw new JWTVerificationException("Access token is missing required claims");
        }
        return jwt;
    }

    public List<String> getRoles(DecodedJWT jwt) {
        return jwt.getClaim(ROLES_CLAIM).asList(String.class);
    }
}
