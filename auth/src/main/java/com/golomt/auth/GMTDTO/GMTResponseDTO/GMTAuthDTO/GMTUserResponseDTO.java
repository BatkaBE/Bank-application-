package com.golomt.auth.GMTDTO.GMTResponseDTO.GMTAuthDTO;


import com.golomt.auth.GMTConstant.GMTUserRole;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTUserResponseDTO implements GMTGeneralDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String customerId;
    private String userStatus;
    private Boolean isActive;
    private Set<GMTUserRole> roles;
    private LocalDateTime createdAt;
}