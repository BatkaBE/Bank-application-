package com.golomt.auth.GMTHelper;

import com.golomt.auth.GMTConstant.GMTCommon;
import com.golomt.auth.GMTConstant.GMTConstants;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTHeaderDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import net.minidev.json.JSONArray;

import java.time.LocalDateTime;
import java.util.List;

public class GMTResponse<E> implements GMTConstants {
    private GMTGeneralDTO dto;
    private List<E> list;
    private GMTResponseDTO responseDTO;
    private JSONArray array;
    private Object obj;

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

    public GMTResponse(int value, String s) {
        this.responseDTO = new GMTResponseDTO();
        this.responseDTO.setHeader(new GMTHeaderDTO());
        this.responseDTO.getHeader().setErrorCode(value);
        this.responseDTO.getHeader().setStatus((value >= 200 && value < 300) ? GMTCommon.SUCCESS.value() : GMTCommon.FAILED.value());
        this.responseDTO.getHeader().setMessage(s);
        this.responseDTO.getHeader().setTimestamp(LocalDateTime.now());
    }
    public GMTResponse(int code, String message, Object obj) {
        this.responseDTO = new GMTResponseDTO();
        this.responseDTO.setHeader(new GMTHeaderDTO());
        this.getResponseDTO().getHeader().setErrorCode(code);
        this.getResponseDTO().getHeader().setStatus((code >= 200 && code < 300) ? GMTCommon.SUCCESS.value() : GMTCommon.FAILED.value());
        this.getResponseDTO().getHeader().setMessage(message);
        this.getResponseDTO().getHeader().setTimestamp(LocalDateTime.now());
        this.obj = obj;
    }

    public GMTResponseDTO getResponse() {
        if (dto != null) {
            this.getResponseDTO().setBody(dto);
        } else if (list != null) {
            this.getResponseDTO().setBody(list);
        } else if (array != null) {
            this.getResponseDTO().setBody(array);
        } else if (obj != null) {
            this.getResponseDTO().setBody(obj);
        }
        return this.getResponseDTO();
    }

    public GMTResponseDTO getError() {
        return getResponse();
    }

    public GMTResponseDTO getResponseDTO() {
        return responseDTO != null ? this.responseDTO : (this.responseDTO = new GMTResponseDTO());
    }
}