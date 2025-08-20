package com.golomt.auth.GMTDTO.GMTResponseDTO.GMTAuthDTO;

import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GMTAccessTokenResponseDTO  implements GMTGeneralDTO {
    private String accessToken;
    private long expiresIn;

    public GMTAccessTokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
        this.expiresIn = 3600;
    }
}
