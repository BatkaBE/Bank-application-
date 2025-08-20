package com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTCommonDTO;

import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTHeaderDTO;

import javax.validation.constraints.NotNull;

public class GMTRequestDTO<E>{
    @NotNull
    private GMTHeaderDTO header;
    @NotNull
    private E body;
    public GMTRequestDTO(GMTHeaderDTO header, E body) {
        this.header = header;
        this.body = body;
    }

    public GMTRequestDTO() {
        this.header = new GMTHeaderDTO();
        this.body = null;
    }

    public GMTHeaderDTO getHeader() {
        return header;
    }
    public void setHeader(GMTHeaderDTO header) {
        this.header = header;
    }
    public E getBody() {
        return body;
    }
    public void setBody(E body) {
        this.body = body;
    }
}
