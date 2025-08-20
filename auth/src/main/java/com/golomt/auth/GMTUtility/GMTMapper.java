package com.golomt.auth.GMTUtility;

import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTAuthDTO.GMTUserResponseDTO;
import com.golomt.auth.GMTEntity.GMTUserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GMTMapper {
    public static GMTUserResponseDTO mapToResponse(GMTUserEntity user) {
        GMTUserResponseDTO userResponseDTO = new GMTUserResponseDTO();
        userResponseDTO.setUsername(user.getUsername());
        userResponseDTO.setFirstName(user.getFirstName());
        userResponseDTO.setLastName(user.getLastName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setPhoneNumber(user.getPhoneNumber());
        userResponseDTO.setRoles(user.getRoles());;
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFullName(user.getFullName());
        userResponseDTO.setCustomerId(user.getCustomerId());
        userResponseDTO.setRoles(user.getRoles());
        userResponseDTO.setCreatedAt(user.getCreatedAt());
        return  userResponseDTO;
    }
    public static List<GMTUserResponseDTO> mapToResponseList(List<GMTUserEntity> users) {
        List<GMTUserResponseDTO> responses = new ArrayList<GMTUserResponseDTO>();
        for (GMTUserEntity user : users) {
            GMTUserResponseDTO userResponseDTO = new GMTUserResponseDTO();
            userResponseDTO.setUsername(user.getUsername());
            userResponseDTO.setFirstName(user.getFirstName());
            userResponseDTO.setLastName(user.getLastName());
            userResponseDTO.setEmail(user.getEmail());
            userResponseDTO.setPhoneNumber(user.getPhoneNumber());
            userResponseDTO.setRoles(user.getRoles());;
            userResponseDTO.setEmail(user.getEmail());
            userResponseDTO.setId(user.getId());
            userResponseDTO.setFullName(user.getFullName());
            userResponseDTO.setCustomerId(user.getCustomerId());
            userResponseDTO.setRoles(user.getRoles());
            userResponseDTO.setCreatedAt(user.getCreatedAt());
            responses.add(userResponseDTO);
        }
        return responses;
    }

}
