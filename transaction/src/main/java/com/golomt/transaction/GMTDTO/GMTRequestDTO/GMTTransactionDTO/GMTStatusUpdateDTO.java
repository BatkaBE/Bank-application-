package com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMTStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}