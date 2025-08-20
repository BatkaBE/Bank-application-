package com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO;

import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTLoginRequestDTO implements GMTGeneralDTO {

    private String username;
    private String password;

    private boolean rememberMe;
}