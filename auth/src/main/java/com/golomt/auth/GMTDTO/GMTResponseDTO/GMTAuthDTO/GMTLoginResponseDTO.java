
package com.golomt.auth.GMTDTO.GMTResponseDTO.GMTAuthDTO;

import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTLoginResponseDTO implements GMTGeneralDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private GMTUserResponseDTO user;
}