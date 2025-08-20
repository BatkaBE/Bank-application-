package com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTHeaderDTO {
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private int errorCode;

    public GMTHeaderDTO(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}