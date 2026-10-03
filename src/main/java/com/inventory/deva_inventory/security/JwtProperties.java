package com.inventory.deva_inventory.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("deva-inventory") String issuer,
        @DefaultValue("30m") Duration accessTokenExpiration,
        @DefaultValue("24h") Duration refreshTokenExpiration) {
}
