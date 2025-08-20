
package com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTBalanceUpdateDTO {

    @NotNull(message = "Amount delta is required")
    private Double amountDelta;

    private String reason;
}