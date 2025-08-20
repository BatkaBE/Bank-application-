package com.golomt.auth.GMTController;

import com.golomt.auth.GMTConstant.GMTLog;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.GMTLoginRequestDTO;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.GMTRefreshTokenRequestDTO;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.GMTUserRegistrationRequestDTO;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.GMTUserUpdateRequestDTO;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTErrorDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.auth.GMTException.*;
import com.golomt.auth.GMTHelper.GMTResponse;
import com.golomt.auth.GMTService.GMTAuthService;
import com.golomt.auth.GMTUtility.GMTLOGUtilities;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GMTAuthController {
    private final GMTAuthService authService;

    @PostMapping("/login")
    public GMTResponseDTO doLogin(
            @RequestBody GMTRequestDTO<GMTLoginRequestDTO> dto,
            HttpServletRequest req) throws GMTRMIException, GMTBusinessException, GMTValidationException {
        String ipAddress = req.getRemoteAddr();
        String deviceInfo = req.getRemoteHost();
        String userAgent = req.getHeader("User-Agent");
        String username = dto.getBody().getUsername().toLowerCase();
        String remoteUser = req.getRemoteUser() != null ? req.getRemoteUser() : "anonymous";

        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + username + "][init][" + remoteUser + "]");
            GMTResponseDTO response = authService.authenticate(dto.getBody(), ipAddress, userAgent);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + username + "][end][" + remoteUser + "]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + username + "][rmi][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + username + "][unknown][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @PostMapping("/refresh")
    public GMTResponseDTO refreshToken(
            @RequestBody GMTRequestDTO<GMTRefreshTokenRequestDTO> dto,
            HttpServletRequest req) throws GMTRMIException, GMTBusinessException, GMTValidationException {
            String remoteUser = req.getRemoteUser() != null ? req.getRemoteUser() : "anonymous";
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + dto.getBody() + "][init][" + remoteUser + "]");
            GMTResponseDTO response = authService.refreshToken(dto.getBody(),req);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + dto.getBody() + "][end][" + remoteUser + "]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + dto.getBody() + "][rmi][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.authenticate][" + dto.getBody() + "][unknown][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }


    @PostMapping("/register")
    public GMTResponseDTO registerUser(
            @RequestBody GMTRequestDTO<GMTUserRegistrationRequestDTO> dto,
            HttpServletRequest req) {
        String ipAddress = req.getRemoteAddr();
        String userAgent = req.getHeader("User-Agent");
        String username = dto.getBody().getUsername().toLowerCase();
        String remoteUser = req.getRemoteUser() != null ? req.getRemoteUser() : "anonymous";

        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.registerUser][" + username + "][init][" + remoteUser + "]");
            String createdBy = remoteUser;
            GMTResponseDTO response = authService.registerUser(dto.getBody(), createdBy);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.registerUser][" + username + "][end][" + remoteUser + "]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.registerUser][" + username + "][rmi][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.registerUser][" + username + "][runtime][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.registerUser][" + username + "][unknown][" + remoteUser + "] " + e.getMessage());
                return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @PostMapping("/logout")
    public GMTResponseDTO logout(@RequestHeader("Authorization") String authHeader) {
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.logout][init][null]");
            String token = authHeader.replace("Bearer ", "").trim();
            GMTResponseDTO response = authService.logout(token);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.logout][end][null]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.logout][rmi][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.logout][runtime][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.logout][unknown][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @DeleteMapping("/{userId}")
    public GMTResponseDTO deleteUser(@PathVariable Long userId, HttpServletRequest req) {
        String remoteUser = req.getRemoteUser() != null ? req.getRemoteUser() : "system";

        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.deleteUser][" + userId + "][init][" + remoteUser + "]");
            String deletedBy = remoteUser;
            GMTResponseDTO response = authService.deleteUser(userId, deletedBy);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.deleteUser][" + userId + "][end][" + remoteUser + "]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.deleteUser][" + userId + "][rmi][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.deleteUser][" + userId + "][runtime][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.deleteUser][" + userId + "][unknown][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @GetMapping("/user/id/{id}")
    public GMTResponseDTO getUserById(@PathVariable Long id) {
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getUserById][" + id + "][init][null]");
            GMTResponseDTO response = authService.getUserById(id);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getUserById][" + id + "][end][null]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserById][" + id + "][rmi][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserById][" + id + "][runtime][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserById][" + id + "][unknown][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @GetMapping("/user/username/{username}")
    public GMTResponseDTO getUserByUsername(@PathVariable String username) {
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getUserByUsername][" + username + "][init][null]");
            GMTResponseDTO response = authService.getUserByUsername(username.toLowerCase());
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getUserByUsername][" + username + "][end][null]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserByUsername][" + username + "][rmi][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserByUsername][" + username + "][runtime][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserByUsername][" + username + "][unknown][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @GetMapping("/user/email/{email}")
    public GMTResponseDTO getUserByEmail(@PathVariable String email) {
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getUserByEmail][" + email + "][init][null]");
            GMTResponseDTO response = authService.getUserByEmail(email.toLowerCase());
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getUserByEmail][" + email + "][end][null]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserByEmail][" + email + "][rmi][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserByEmail][" + email + "][runtime][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getUserByEmail][" + email + "][unknown][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @GetMapping("/users")
    public GMTResponseDTO getAllUsers() {
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getAllUsers][init][null]");
            GMTResponseDTO response = authService.getAllUsers();
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.getAllUsers][end][null]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getAllUsers][rmi][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getAllUsers][runtime][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.getAllUsers][unknown][null] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        }
    }

    @PutMapping("/user/{id}")
    public GMTResponseDTO updateUser(@PathVariable Long id, @RequestBody GMTRequestDTO<GMTUserUpdateRequestDTO> dto, HttpServletRequest req) {
        String remoteUser = req.getRemoteUser() != null ? req.getRemoteUser() : "system";
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.updateUser][" + id + "][init][" + remoteUser + "]");
            GMTResponseDTO response = authService.updateUser(id, dto.getBody(), remoteUser);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[controller][auth.updateUser][" + id + "][end][" + remoteUser + "]");
            return response;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[controller][auth.updateUser][" + id + "][unknown][" + remoteUser + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        }
    }
}