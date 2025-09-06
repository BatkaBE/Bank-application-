package com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTResponseDTO {
    private Integer status;
    private String message;
    private Object data;
    private String timestamp;
}
