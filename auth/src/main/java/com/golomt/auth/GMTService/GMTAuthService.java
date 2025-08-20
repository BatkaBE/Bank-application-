package com.golomt.auth.GMTService;

import com.golomt.auth.GMTConstant.GMTUserRole;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.*;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.auth.GMTException.GMTCustomException;
import com.golomt.auth.GMTException.GMTValidationException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;

public interface GMTAuthService {
    
    GMTResponseDTO authenticate(GMTLoginRequestDTO loginRequest, String ipAddress, String userAgent) throws GMTCustomException;

    GMTResponseDTO logout(String token) throws GMTCustomException;
    
    GMTResponseDTO registerUser(GMTUserRegistrationRequestDTO registrationRequest, String createdBy) throws GMTCustomException, GMTValidationException;

    GMTResponseDTO deleteUser(Long userId, String deletedBy) throws GMTCustomException;

    GMTResponseDTO getUserById(Long id) throws GMTCustomException;

    GMTResponseDTO getUserByUsername(String username) throws GMTCustomException;

    GMTResponseDTO getUserByEmail(String email) throws GMTCustomException;

    GMTResponseDTO getAllUsers() throws GMTCustomException;

    GMTResponseDTO getUsersByRole(GMTUserRole role) throws GMTCustomException;

    boolean existsByUsername(String username) throws GMTCustomException;

    boolean existsByEmail(String email) throws GMTCustomException;


    GMTResponseDTO refreshToken(GMTRefreshTokenRequestDTO refreshTokenRequest, HttpServletRequest req) throws GMTCustomException;

    GMTResponseDTO updateUser(Long id, GMTUserUpdateRequestDTO updateRequest, String updatedBy) throws GMTCustomException, GMTValidationException;











//    // Password Management
//    void changePassword(GMTChangePasswordRequestDTO changePasswordRequest, String username);
//
//    void resetPassword(GMTResetPasswordRequestDTO resetPasswordRequest);

}