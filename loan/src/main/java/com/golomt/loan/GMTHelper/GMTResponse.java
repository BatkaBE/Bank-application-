package com.golomt.loan.GMTHelper;

import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class GMTResponse {
    private Integer status;
    private String message;
    private String transactionId;
    private Object data;
    private String timestamp;

    public GMTResponse(Integer status, String message, String transactionId, Object data) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public GMTResponse(Integer status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public GMTResponseDTO getResponseDTO() {
        return new GMTResponseDTO(this.status, this.message, this.data, this.timestamp);
    }
}
