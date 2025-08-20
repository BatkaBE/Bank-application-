package com.golomt.transaction.GMTHelper;

import com.golomt.transaction.GMTConstant.GMTCommon;
import com.golomt.transaction.GMTConstant.GMTConstants;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTHeaderDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import net.minidev.json.JSONArray;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;

import java.time.LocalDateTime;
import java.util.List;

public class GMTResponse<E> implements GMTConstants {
    private GMTGeneralDTO dto;
    private List<E> list;
    private GMTResponseDTO responseDTO;
    private JSONArray array;

    public GMTResponse(int code, String message, GMTGeneralDTO generalDTO) {
        this.responseDTO = new GMTResponseDTO();
        this.responseDTO.setHeader(new GMTHeaderDTO());
        this.getResponseDTO().getHeader().setErrorCode(code);
        this.getResponseDTO().getHeader().setStatus((code >= 200 && code < 300) ? GMTCommon.SUCCESS.value() : GMTCommon.FAILED.value());
        this.getResponseDTO().getHeader().setMessage(message);
        this.getResponseDTO().getHeader().setTimestamp(LocalDateTime.now());
        dto = generalDTO;
        this.responseDTO.setBody(generalDTO);
    }
    public GMTResponse(int code, String message, List<E> list) {
        this.responseDTO = new GMTResponseDTO();
        this.responseDTO.setHeader(new GMTHeaderDTO());
        this.getResponseDTO().getHeader().setErrorCode(code);
        this.getResponseDTO().getHeader().setStatus((code >= 200 && code < 300) ? GMTCommon.SUCCESS.value() : GMTCommon.FAILED.value());
        this.getResponseDTO().getHeader().setMessage(message);
        this.getResponseDTO().getHeader().setTimestamp(LocalDateTime.now());
        this.list = list;
        this.responseDTO.setBody(list);
    }
    public GMTResponse(int code, String message, String transactionId,GMTGeneralDTO generalDTO) {
        this.responseDTO = new GMTResponseDTO();
        this.responseDTO.setHeader(new GMTHeaderDTO());
        this.getResponseDTO().getHeader().setErrorCode(code);
        this.getResponseDTO().getHeader().setStatus((code >= 200 && code < 300) ? GMTCommon.SUCCESS.value() : GMTCommon.FAILED.value());
        this.getResponseDTO().getHeader().setMessage(message);
        this.getResponseDTO().getHeader().setTimestamp(LocalDateTime.now());
        this.getResponseDTO().getHeader().setTransactionId(transactionId);
        dto = generalDTO;
        this.responseDTO.setBody(generalDTO);
    }

    public GMTResponse(int code, String message, JSONArray array) {
        this.responseDTO = new GMTResponseDTO();
        this.responseDTO.setHeader(new GMTHeaderDTO());
        this.getResponseDTO().getHeader().setErrorCode(code);
        this.getResponseDTO().getHeader().setStatus((code >= 200 && code < 300) ? GMTCommon.SUCCESS.value() : GMTCommon.FAILED.value());
        this.getResponseDTO().getHeader().setMessage(message);
        this.getResponseDTO().getHeader().setTimestamp(LocalDateTime.now());
        this.array = array;
        this.responseDTO.setBody(array);
    }


    public GMTResponseDTO getResponse() {
        this.getResponseDTO().setBody(dto);
        return this.getResponseDTO();
    }

    public GMTResponseDTO getError() {
        this.getResponseDTO().setBody(dto);
        return this.getResponseDTO();
    }

    public GMTResponseDTO getResponseDTO() {
        return responseDTO != null ? this.responseDTO : (this.responseDTO = new GMTResponseDTO());
    }
}
