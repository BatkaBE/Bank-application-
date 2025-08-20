package com.golomt.account.GMTDTO.GMTResponseDTO.GMTAccountDTO;

import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTAccountResponseDTO implements GMTGeneralDTO {

    private Long id;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private Double balance;
    private String currencyCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private boolean isLocked;
    private String createdBy;
}