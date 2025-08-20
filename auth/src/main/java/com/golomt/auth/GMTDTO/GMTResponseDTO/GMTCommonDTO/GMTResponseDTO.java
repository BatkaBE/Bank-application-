package com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO;

public class GMTResponseDTO implements GMTGeneralDTO {
    private GMTHeaderDTO header;

    private Object body;

    public GMTResponseDTO(GMTHeaderDTO header, Object body) {
        this.header = header;
        this.body = body;
    }
    public GMTResponseDTO() {
        this.header = new GMTHeaderDTO();
    }


    public GMTResponseDTO(GMTHeaderDTO header) {
        this.header = header;
    }


    public GMTHeaderDTO getHeader() {
        return header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public void setHeader(GMTHeaderDTO header) {
        this.header = header;
    }
}