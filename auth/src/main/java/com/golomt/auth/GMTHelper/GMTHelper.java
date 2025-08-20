package com.golomt.auth.GMTHelper;

import com.golomt.auth.GMTConstant.GMTTokenType;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.GMTUserRegistrationRequestDTO;
import com.golomt.auth.GMTEntity.GMTTokenEntity;
import com.golomt.auth.GMTEntity.GMTUserEntity;
import com.golomt.auth.GMTException.GMTCustomException;
import com.golomt.auth.GMTException.GMTValidationException;
import com.golomt.auth.GMTRepository.GMTTokenRepository;
import com.golomt.auth.GMTService.GMTJWTService;
import org.springframework.stereotype.Component;



import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Component
public class GMTHelper {

    private final GMTJWTService jwtService;
    private final GMTTokenRepository tokenRepository;

    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public GMTHelper(GMTJWTService jwtService, GMTTokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    public void saveToken(String tokenValue, GMTTokenType tokenType, GMTUserEntity user, String ipAddress, String userAgent) {
        LocalDateTime expiresAt = tokenType == GMTTokenType.ACCESS_TOKEN
                ? LocalDateTime.now().plusSeconds(jwtService.getAccessTokenExpiration())
                : LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration());

        GMTTokenEntity tokenEntity = new GMTTokenEntity(tokenValue, tokenType, user.getId(), user.getUsername(), expiresAt);
        tokenEntity.setIpAddress(ipAddress);
        tokenEntity.setUserAgent(userAgent);

        tokenRepository.save(tokenEntity);
    }


    public void validateRegistrationRequest(GMTUserRegistrationRequestDTO request) throws GMTCustomException, GMTValidationException {
        if (!validateUsername(request.getUsername())) {
            throw new GMTValidationException("Invalid username format");
        }

        if (!validateEmail(request.getEmail())) {
            throw new GMTValidationException("Invalid email format");
        }

        validatePassword(request.getPassword());

        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new GMTValidationException("First name is required");
        }

        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new GMTValidationException("Last name is required");
        }
    }

    private void validatePassword(String password) throws GMTCustomException, GMTValidationException {
        if (password == null || password.length() < 8) {
            throw new GMTValidationException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new GMTValidationException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new GMTValidationException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new GMTValidationException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new GMTValidationException("Password must contain at least one special character");
        }
    }

    public boolean validateEmail(String email) {
        return email != null && pattern.matcher(email).matches();
    }

    public boolean validateUsername(String username) {
        return username != null &&
                username.length() >= 3 &&
                username.length() <= 50 &&
                username.matches("^[a-zA-Z0-9._-]+$");
    }
}
