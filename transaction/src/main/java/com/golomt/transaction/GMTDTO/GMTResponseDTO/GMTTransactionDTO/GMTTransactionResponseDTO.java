package com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTTransactionDTO;

import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTTransactionResponseDTO implements GMTGeneralDTO {

    private Long id;
    private String transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Double amount;
    private String currencyCode;
    private String transactionType;
    private String status;
    private String description;
    private String reference;
    private Double fee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime processedAt;
    private String failureReason;
    private String toUserId;
}