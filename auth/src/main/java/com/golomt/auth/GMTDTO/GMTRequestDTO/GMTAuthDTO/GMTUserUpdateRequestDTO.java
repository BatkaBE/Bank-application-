package com.golomt.auth.GMTDTO.GMTRequestDTO.GMTAuthDTO;

import lombok.Data;

@Data
public class GMTUserUpdateRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}


