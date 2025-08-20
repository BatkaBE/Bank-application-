package com.golomt.account.GMTUtility;

import com.golomt.account.GMTDTO.GMTResponseDTO.GMTAccountDTO.GMTAccountResponseDTO;
import com.golomt.account.GMTEntity.GMTAccountEntity;

public class GMTMapper {
    public static GMTAccountResponseDTO mapToResponse(GMTAccountEntity entity) {
        GMTAccountResponseDTO dto = new GMTAccountResponseDTO();
        dto.setId(entity.getId());
        dto.setBalance(entity.getBalance());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setAccountType(entity.getAccountType());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setActive(entity.isActive());
        dto.setLocked(entity.isLocked());
        dto.setAccountName(entity.getAccountName());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }
}
