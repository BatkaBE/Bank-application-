package com.golomt.auth.GMTServiceImple;

import com.golomt.auth.GMTEntity.GMTUserEntity;
import com.golomt.auth.GMTService.GMTJWTService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GMTJWTServiceImpl implements GMTJWTService {

    private static final long ACCESS_TOKEN_EXPIRATION = 1800;
    private static final long REFRESH_TOKEN_EXPIRATION = 302400;
    private static final long PASSWORD_RESET_EXPIRATION = 1800;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.jwtExpirationInMs}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateAccessToken(GMTUserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("customerId", user.getCustomerId());
        claims.put("roles", user.getRoles().stream().map(Enum::name).collect(Collectors.toList()));
        claims.put("tokenType", "ACCESS");

        return createToken(claims, user.getUsername(), ACCESS_TOKEN_EXPIRATION);
    }

    @Override
    public String generateRefreshToken(GMTUserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tokenType", "REFRESH");

        return createToken(claims, user.getUsername(), REFRESH_TOKEN_EXPIRATION);
    }

    @Override
    public String generatePasswordResetToken(GMTUserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "PASSWORD_RESET");

        return createToken(claims, user.getUsername(), PASSWORD_RESET_EXPIRATION);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed: {}, reason: {}", token, e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @Override
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse claims from token: {}, reason: {}", token, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    @Override
    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION;
    }

    @Override
    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
}