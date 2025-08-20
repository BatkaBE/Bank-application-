package com.golomt.account.GMTDTO.GMTRequestDTO.GMTCommonDTO;

import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTHeaderDTO;
import org.jetbrains.annotations.NotNull;

public class GMTRequestDTO<E>{
    @NotNull
    private GMTHeaderDTO header;
    @NotNull
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
