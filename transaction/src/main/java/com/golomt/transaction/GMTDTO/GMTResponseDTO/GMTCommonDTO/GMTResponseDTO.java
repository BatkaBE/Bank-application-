package com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO;

public class GMTResponseDTO implements GMTGeneralDTO {

    private GMTHeaderDTO header;
    private Object body;

    /**
     * Constructor
     */
    public GMTResponseDTO(GMTHeaderDTO header, Object body) {
        this.header = header;
        this.body = body;
    }

    public GMTResponseDTO(GMTHeaderDTO header) {
        this.header = header;
    }

    public GMTResponseDTO() {
        super();
    }

    /**
     * Getter Setter - Changed from private to public
     */

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