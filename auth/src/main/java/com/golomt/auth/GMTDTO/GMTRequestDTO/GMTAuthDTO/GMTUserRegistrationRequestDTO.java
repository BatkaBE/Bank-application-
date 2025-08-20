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
public class GMTUserRegistrationRequestDTO implements GMTGeneralDTO {

    private String username;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String phoneNumber;
}