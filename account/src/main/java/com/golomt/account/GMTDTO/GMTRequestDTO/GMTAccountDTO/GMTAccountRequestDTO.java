package com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTAccountRequestDTO {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    @NotBlank(message = "Account type is required")
    @Size(max = 50, message = "Account type must not exceed 50 characters")
    private String accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
    private Double balance;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    private String fromAccountNumber;

    private boolean isSystemcreated = false;
}