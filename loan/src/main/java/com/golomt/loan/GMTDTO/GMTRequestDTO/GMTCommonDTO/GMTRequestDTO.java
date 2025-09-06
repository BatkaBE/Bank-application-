package com.golomt.loan.GMTDTO.GMTRequestDTO.GMTCommonDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTRequestDTO<T> {
    private T data;
}
