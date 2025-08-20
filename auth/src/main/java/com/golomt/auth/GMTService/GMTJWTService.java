package com.golomt.auth.GMTService;

import com.golomt.auth.GMTEntity.GMTUserEntity;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

public interface GMTJWTService {

    String generateAccessToken(GMTUserEntity user);

    String generateRefreshToken(GMTUserEntity user);

    String generatePasswordResetToken(GMTUserEntity user);

    boolean validateToken(String token);

    String getUsernameFromToken(String token);

    Claims getClaimsFromToken(String token);

    boolean isTokenExpired(String token);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();
}