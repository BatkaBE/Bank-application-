package com.golomt.loan.GMTDTO.GMTRequestDTO.GMTLoanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTLoanRequestDTO {
    
    @NotNull(message = "Хэрэглэгчийн ID заавал оруулах")
    private String userId;
    
    @NotNull(message = "Дансны дугаар заавал оруулах")
    private String accountNumber;
    
    @NotNull(message = "Зээлийн хэмжээ заавал оруулах")
    @Positive(message = "Зээлийн хэмжээ эерэг байх ёстой")
    private Double loanAmount;
    
    @NotNull(message = "Хүүгийн түвшин заавал оруулах")
    @Positive(message = "Хүүгийн түвшин эерэг байх ёстой")
    private Double interestRate;
    
    @NotNull(message = "Зээлийн хугацаа заавал оруулах")
    @Positive(message = "Зээлийн хугацаа эерэг байх ёстой")
    private Integer loanTerm;
    
    @NotNull(message = "Зээлийн төрөл заавал оруулах")
    private String loanType;
    
    @NotNull(message = "Зээлийн зорилго заавал оруулах")
    @Size(min = 10, max = 500, message = "Зээлийн зорилго 10-500 тэмдэгт байх ёстой")
    private String purpose;
    
    @NotNull(message = "Валютын код заавал оруулах")
    private String currencyCode;
    
    private String collateral;
    
    private String guarantor;
    
    private String notes;
}
