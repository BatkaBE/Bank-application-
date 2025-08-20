
package com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO;

import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTTransactionRequestDTO implements GMTGeneralDTO {

    @NotBlank(message = "From account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "To account number is required")
    private String toAccountNumber;

    @NotBlank(message = "To account number is required")
    private String toUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    private String description;

    private String reference;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fee must be non-negative")
    private Double fee;
}