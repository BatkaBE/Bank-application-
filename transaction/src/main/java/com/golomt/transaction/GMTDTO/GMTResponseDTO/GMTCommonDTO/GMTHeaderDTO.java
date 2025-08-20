package com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTHeaderDTO {
    private String status;
    private String message;
    private String transactionId;
    private LocalDateTime timestamp;
    private int errorCode;

    public GMTHeaderDTO(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}