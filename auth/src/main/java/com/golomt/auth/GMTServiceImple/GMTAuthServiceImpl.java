package com.golomt.auth.GMTServiceImple;

import com.golomt.auth.GMTConstant.GMTLog;
import com.golomt.auth.GMTConstant.GMTTokenType;
import com.golomt.auth.GMTConstant.GMTUserRole;
import com.golomt.auth.GMTConstant.GMTUserStatus;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import com.golomt.auth.GMTEntity.*;
import com.golomt.auth.GMTException.*;
import com.golomt.auth.GMTHelper.GMTHelper;
import com.golomt.auth.GMTHelper.GMTResponse;
import com.golomt.auth.GMTRepository.GMTUserRepository;
import com.golomt.auth.GMTRepository.GMTTokenRepository;
import com.golomt.auth.GMTService.GMTAuthService;
import com.golomt.auth.GMTService.GMTJWTService;
import com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO.*;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTAuthDTO.*;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.auth.GMTUtility.GMTAuthUtilities;
import com.golomt.auth.GMTUtility.GMTLOGUtilities;
import com.golomt.auth.GMTUtility.GMTMapper;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Transactional
@Service
public class GMTAuthServiceImpl implements GMTAuthService {
    private final GMTUserRepository userRepository;
    private final GMTTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final GMTJWTService jwtService;
    private final GMTHelper helper;
    private final GMTJWTServiceImpl gMTJWTServiceImpl;

    @Inject
    public GMTAuthServiceImpl(GMTUserRepository userRepository,
                              GMTTokenRepository tokenRepository,
                              PasswordEncoder passwordEncoder,
                              GMTJWTService jwtService,
                              GMTHelper helper, GMTJWTServiceImpl gMTJWTServiceImpl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.helper = helper;
        this.gMTJWTServiceImpl = gMTJWTServiceImpl;
    }

    @Override
    public GMTResponseDTO authenticate(GMTLoginRequestDTO loginRequest, String ipAddress, String userAgent) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.authenticate][" + loginRequest.getUsername().toLowerCase() + "][init]");

        try {
            Optional<GMTUserEntity> optionalUser = userRepository.findByUsernameOrEmail(
                    loginRequest.getUsername(), loginRequest.getUsername());
            if (optionalUser.isEmpty()) {
                return new GMTResponse(HttpStatus.UNAUTHORIZED.value(), "Нэвтрэх нэр буруу", (GMTGeneralDTO) null).getResponseDTO();
            }
            GMTUserEntity user = optionalUser.get();
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                return new GMTResponse(HttpStatus.UNAUTHORIZED.value(), "Нууц үг буруу", (GMTGeneralDTO) null).getResponseDTO();
            }

            userRepository.save(user);
            GMTLOGUtilities.debug(GMTLog.AUTH.getValue(), "respond: " + user);
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            helper.saveToken(accessToken, GMTTokenType.ACCESS_TOKEN, user, ipAddress, userAgent);
            helper.saveToken(refreshToken, GMTTokenType.REFRESH_TOKEN, user, ipAddress, userAgent);

            GMTUserResponseDTO userResponse = GMTMapper.mapToResponse(user);
            if (userResponse == null) {
                GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.authenticate][" + userResponse.getUsername() + "][Хэрэглэгч олдсонгүй] ");
                throw new GMTCustomException("Хэрэглэгч олдсонгүй");
            }

            GMTLoginResponseDTO response = new GMTLoginResponseDTO(
                    accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpiration(), userResponse);
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "амжилттай нэвтэрлээ", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.authenticate][" + responseDTO.toString().toLowerCase() + "][end]");

            return responseDTO;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.authenticate][" + loginRequest.getUsername().toLowerCase() + "][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.authenticate][" + loginRequest.getUsername().toLowerCase() + "][rmi] " + e.getMessage());
            throw e;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.authenticate][" + loginRequest.getUsername().toLowerCase() + "][unknown] " + e.getMessage());
            throw new GMTCustomException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public GMTResponseDTO refreshToken(GMTRefreshTokenRequestDTO refreshTokenRequest, HttpServletRequest req) throws GMTCustomException{
        try {
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.refreshToken][" + refreshTokenRequest.getRefreshToken() + "][init]");

            if(refreshTokenRequest.getRefreshToken() == null || refreshTokenRequest.getRefreshToken().isEmpty())
            {
                throw new GMTCustomException("refresh token хоосон байна");
            }
            if(gMTJWTServiceImpl.validateToken(refreshTokenRequest.getRefreshToken())){
                String username = gMTJWTServiceImpl.getUsernameFromToken(refreshTokenRequest.getRefreshToken());
                Optional<GMTUserEntity> optionalUser = userRepository.findByUsername(username);
                if (optionalUser.isEmpty()) {
                    throw new GMTCustomException("Хэрэглэгч олдсонгүй");
                }
                GMTUserEntity user = optionalUser.get();

                String accessToken = jwtService.generateAccessToken(user);
                GMTAccessTokenResponseDTO response = new GMTAccessTokenResponseDTO(accessToken);
                return new GMTResponse(HttpStatus.OK.value(), "Access token шинэчлэгдлээ...", response).getResponseDTO();
            }
            return new GMTResponse(HttpStatus.FORBIDDEN.value(), "Access token шинэчлэж чадсангүй").getResponseDTO();
        }
        catch (GMTCustomException e)
        {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.refreshToken][" + refreshTokenRequest.getRefreshToken() + "][custom] " + e.getMessage());
            throw e;
        }
    }

    @Override
    public GMTResponseDTO logout(String token) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.logout][init]");
        try {
            Optional<GMTTokenEntity> optionalToken = tokenRepository.findByTokenValue(token);
            if (optionalToken.isEmpty()) {
                throw new GMTCustomException("Токен олдсонгүй");
            }
            GMTTokenEntity tokenEntity = optionalToken.get();
            if (tokenEntity.getIsRevoked()) {
                return new GMTResponse(HttpStatus.FORBIDDEN.value(), "Та системээс гарсан байна", (GMTGeneralDTO) null).getResponseDTO();

            } else {
                tokenEntity.setIsRevoked(true);
                tokenRepository.save(tokenEntity);
                GMTLOGUtilities.debug(GMTLog.AUTH.getValue(), "respond: " + tokenEntity);

                GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.logout][end]");
                return new GMTResponse(HttpStatus.OK.value(), "Амжилтай системээс гарлаа", (GMTGeneralDTO) null).getResponseDTO();
            }
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.logout][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.logout][rmi] " + e.getMessage());
            throw new GMTRMIException("RMI error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.logout][unknown] " + e.getMessage());
            throw new GMTCustomException("Logout failed: " + e.getMessage());
        }
    }

    @Override
    public GMTResponseDTO registerUser(GMTUserRegistrationRequestDTO registrationRequest, String createdBy) throws GMTCustomException, GMTValidationException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.registerUser][" + registrationRequest.getUsername().toLowerCase() + "][init]");

        try {
            helper.validateRegistrationRequest(registrationRequest);

            if (existsByUsername(registrationRequest.getUsername())) {
                return new GMTResponse(HttpStatus.CONFLICT.value(), "Нэвтрэх нэр ашиглагдсан байна", (GMTGeneralDTO) null).getResponseDTO();

            }
            if (existsByEmail(registrationRequest.getEmail())) {
                return new GMTResponse(HttpStatus.CONFLICT.value(), "Email ашиглагдсан байна", (GMTGeneralDTO) null).getResponseDTO();
            }

            GMTUserEntity user = new GMTUserEntity();
            user.setUsername(registrationRequest.getUsername().toLowerCase());
            user.setEmail(registrationRequest.getEmail().toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(registrationRequest.getPassword()));
            user.setFirstName(registrationRequest.getFirstName());
            user.setLastName(registrationRequest.getLastName());
            user.setPhoneNumber(registrationRequest.getPhoneNumber());
            user.setCreatedBy(createdBy);
            user.setUserStatus(GMTUserStatus.ACTIVE.toString());
            user.setIsActive(true);
            user.setIsLocked(false);
            user.setIsExpired(false);
            user.setRoles(new HashSet<>(Arrays.asList(GMTUserRole.USER)));
            user.setCustomerId(GMTAuthUtilities.generateCustomerId());

            GMTUserEntity savedUser = userRepository.save(user);
            GMTLOGUtilities.debug(GMTLog.AUTH.getValue(),"respond: "+savedUser);

            GMTUserResponseDTO response = GMTMapper.mapToResponse(savedUser);
            if (response == null) {
                throw new GMTCustomException("Хэрэглэгч бүртгэгдсэнгүй");
            }
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.registerUser][" + savedUser.getUsername() + "][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.CREATED.value(), "Амжилттай бүртгүүллээ...", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();
            return responseDTO;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.registerUser][" + registrationRequest.getUsername().toLowerCase() + "][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.registerUser][" + registrationRequest.getUsername().toLowerCase() + "][rmi] " + e.getMessage());
            throw new GMTRMIException("RMI error: " + e.getMessage());
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.registerUser][" + registrationRequest.getUsername().toLowerCase() + "][validation] " + e.getMessage());
            throw new GMTValidationException("Validation error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.registerUser][" + registrationRequest.getUsername().toLowerCase() + "][unknown] " + e.getMessage());
            throw new GMTCustomException("User registration failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO deleteUser(Long userId, String deletedBy) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.deleteUser][" + userId + "][init]");

        try {
            Optional<GMTUserEntity> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return new GMTResponse(HttpStatus.NOT_FOUND.value(), "Хэрэглэгч олдсонгүй", (GMTGeneralDTO) null).getResponseDTO();
            }
            GMTUserEntity user = optionalUser.get();
            user.setIsActive(false);
            user.setUserStatus(GMTUserStatus.DELETED.toString());
            user.setUpdatedBy(deletedBy);

            userRepository.save(user);
            GMTLOGUtilities.debug(GMTLog.AUTH.getValue(),"respond: "+user);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.deleteUser][" + user.getUsername() + "][амжилттай устлаа]");
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.deleteUser][" + userId + "][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "Хэрэглэгч амжилттай устлаа", (GMTGeneralDTO) null);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();
            return responseDTO;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.deleteUser][" + userId + "][rmi] " + e.getMessage());
            throw new GMTRMIException("Delete user RMI error: " + e.getMessage());
        } catch (Exception e) {
            log.error("User deletion error for ID: {}", userId, e);
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.deleteUser][" + userId + "][unknown] " + e.getMessage());
            throw new GMTCustomException("User deletion failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getUserById(Long id) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUserById][" + id + "][init]");

        try {
            Optional<GMTUserEntity> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return new GMTResponse(HttpStatus.NOT_FOUND.value(), "Хэрэглэгч олдсонгүй", (GMTGeneralDTO) null).getResponseDTO();

            }
            GMTUserEntity user = optionalUser.get();
            GMTUserResponseDTO response = GMTMapper.mapToResponse(user);
            if (response == null) {
                throw new GMTCustomException("Хариу ирсэнгүй");
            }

            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUserById][" + id + "][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();

            return responseDTO;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserById][" + id + "][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserById][" + id + "][rmi] " + e.getMessage());
            throw new GMTRMIException("MI error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserById][" + id + "][unknown] " + e.getMessage());
            throw new GMTCustomException("ID failed: " + e.getMessage());
        }
    }

    @Override
    public GMTResponseDTO updateUser(Long id, GMTUserUpdateRequestDTO updateRequest, String updatedBy) throws GMTCustomException, GMTValidationException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.updateUser][" + id + "][init]");
        try {
            GMTUserEntity user = userRepository.findById(id).orElseThrow(() -> new GMTCustomException("Хэрэглэгч олдсонгүй"));

            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
                user.setEmail(updateRequest.getEmail());
            }
            if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().isBlank()) {
                user.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null && !updateRequest.getLastName().isBlank()) {
                user.setLastName(updateRequest.getLastName());
            }
            if (updateRequest.getPhoneNumber() != null) {
                user.setPhoneNumber(updateRequest.getPhoneNumber());
            }
            user.setUpdatedBy(updatedBy);

            GMTUserEntity saved = userRepository.save(user);
            GMTUserResponseDTO response = GMTMapper.mapToResponse(saved);
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.updateUser][" + id + "][end]");
            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай шинэчлэгдлээ", response).getResponseDTO();
        } catch (Exception e) {
            throw new GMTCustomException("Шинэчлэхэд алдаа гарлаа: " + e.getMessage());
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getUserByUsername(String username) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUserByUsername][" + username.toLowerCase() + "][init]");

        try {
            Optional<GMTUserEntity> optionalUser = userRepository.findByUsername(username.toLowerCase());
            if (optionalUser.isEmpty()) {
                return new GMTResponse(HttpStatus.NOT_FOUND.value(), "Хэрэглэгч олдсонгүй", (GMTGeneralDTO) null).getResponseDTO();

            }
            GMTUserEntity user = optionalUser.get();
            GMTUserResponseDTO response = GMTMapper.mapToResponse(user);
            if (response == null) {
                return new GMTResponse(HttpStatus.METHOD_FAILURE.value(), "Хэрэглэгчийн өгөгдлийг зураглаж чадсангүй\n", (GMTGeneralDTO) null).getResponseDTO();

            }

            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUserByUsername][" + username.toLowerCase() + "][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();

            return responseDTO;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserByUsername][" + username.toLowerCase() + "][rmi] " + e.getMessage());
            throw new GMTRMIException("RMI error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserByUsername][" + username.toLowerCase() + "][unknown] " + e.getMessage());
            throw new GMTCustomException("Get user by username failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getUserByEmail(String email) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUserByEmail][" + email.toLowerCase() + "][init]");

        try {
            Optional<GMTUserEntity> optionalUser = userRepository.findByEmail(email.toLowerCase());
            if (optionalUser.isEmpty()) {
                throw new GMTCustomException("Хэрэглэгч олдсонгүй");
            }
            GMTUserEntity user = optionalUser.get();
            GMTUserResponseDTO response = GMTMapper.mapToResponse(user);
            if (response == null) {
                throw new GMTCustomException("Хариу ирсэнгүй");
            }

            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUserByEmail][" + email.toLowerCase() + "][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();

            return responseDTO;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserByEmail][" + email.toLowerCase() + "][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserByEmail][" + email.toLowerCase() + "][rmi] " + e.getMessage());
            throw new GMTRMIException("Get user by email RMI error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUserByEmail][" + email.toLowerCase() + "][unknown] " + e.getMessage());
            throw new GMTCustomException("Get user by email failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAllUsers() throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getAllUsers][init]");

        try {
            List<GMTUserEntity> users = userRepository.findAll();
            List<GMTUserResponseDTO> response = GMTMapper.mapToResponseList(users);
            if (response == null) {
                throw new GMTCustomException("Хариу ирсэнгүй");
            }

            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getAllUsers][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();
            return responseDTO;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getAllUsers][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getAllUsers][rmi] " + e.getMessage());
            throw new GMTRMIException("RMI error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getAllUsers][unknown] " + e.getMessage());
            throw new GMTCustomException("Get all users failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getUsersByRole(GMTUserRole role) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUsersByRole][" + role + "][init]");

        try {
            List<GMTUserEntity> users = userRepository.findByRole(role);
            List<GMTUserResponseDTO> response = GMTMapper.mapToResponseList(users);
            if (response == null) {
                throw new GMTCustomException("Хариу ирсэнгүй");
            }
            GMTLOGUtilities.info(GMTLog.AUTH.getValue(), "[service][auth.getUsersByRole][" + role + "][end]");
            GMTResponse gmtResponse = new GMTResponse(HttpStatus.OK.value(), "", response);
            GMTResponseDTO responseDTO = gmtResponse.getResponseDTO();
            return responseDTO;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUsersByRole][" + role + "][custom] " + e.getMessage());
            throw e;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUsersByRole][" + role + "][rmi] " + e.getMessage());
            throw new GMTRMIException(" error: " + e.getMessage());
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.AUTH.getValue(), "[service][auth.getUsersByRole][" + role + "][unknown] " + e.getMessage());
            throw new GMTCustomException("Get users by role failed: " + e.getMessage());
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsByUsername(String username) {

        return userRepository.existsByUsername(username);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}