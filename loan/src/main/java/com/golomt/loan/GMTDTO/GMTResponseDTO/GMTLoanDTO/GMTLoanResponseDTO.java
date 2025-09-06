package com.golomt.loan.GMTDTO.GMTResponseDTO.GMTLoanDTO;

import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMTLoanResponseDTO implements GMTGeneralDTO {
    private Long id;
    private String loanId;
    private String userId;
    private String accountNumber;
    private Double loanAmount;
    private Double interestRate;
    private Integer loanTerm;
    private String loanType;
    private String status;
    private Double monthlyPayment;
    private Double totalAmount;
    private Double remainingBalance;
    private String currencyCode;
    private String purpose;
    private String collateral;
    private String guarantor;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String rejectionReason;
    private String notes;
}
