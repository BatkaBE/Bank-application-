package com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO;

import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTTransactionUpdateDTO implements GMTGeneralDTO {

    private String status;
    private String failureReason;
    private String updatedBy;
}