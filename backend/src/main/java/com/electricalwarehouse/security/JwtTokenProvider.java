package com.electricalwarehouse.security;

import com.electricalwarehouse.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration accessTtl;

    public JwtTokenProvider(AppProperties props) {
        if (!StringUtils.hasText(props.jwt().secret())) {
            throw new IllegalStateException("JWT_SECRET must be set (base64, >= 256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.jwt().secret()));
        this.accessTtl = Duration.ofMinutes(props.jwt().accessTokenMinutes());
    }

    public String createAccessToken(UserPrincipal user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.id())
                .claim("roles", user.roles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    public long getAccessTtlSeconds() {
        return accessTtl.toSeconds();
    }

    /** Returns the username when the token is valid, empty otherwise. */
    public Optional<String> validateAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
