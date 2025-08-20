package com.golomt.auth.GMTDTO.GMTRequestDTO.GMTCommonDTO;

import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTHeaderDTO;

public class GMTRequestDTO<E>{

    private GMTHeaderDTO header;

    private E body;
    public GMTRequestDTO(GMTHeaderDTO header, E body) {
        this.header = header;
        this.body = body;
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
