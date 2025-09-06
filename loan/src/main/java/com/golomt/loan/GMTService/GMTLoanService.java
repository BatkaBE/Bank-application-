package com.golomt.loan.GMTService;

import com.golomt.loan.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.loan.GMTDTO.GMTRequestDTO.GMTLoanDTO.GMTLoanRequestDTO;
import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.loan.GMTEntity.GMTLoanEntity;
import com.golomt.loan.GMTException.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

public interface GMTLoanService {

    public GMTLoanEntity createLoan(GMTLoanEntity loan) throws GMTCustomException;

    public GMTResponseDTO applyForLoan(GMTRequestDTO<GMTLoanRequestDTO> dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException, GMTRuntimeException;

    public GMTResponseDTO getLoanByLoanId(String loanId, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException;

    public GMTResponseDTO getAllLoansByUser(String userId, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException, GMTValidationException;

    public GMTResponseDTO getLoansByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

    public GMTResponseDTO getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

    public GMTResponseDTO getLoansByAmountRange(Double minAmount, Double maxAmount, HttpServletRequest req);

    public GMTResponseDTO updateLoanStatus(String loanId, String status, String updatedBy, String rejectionReason, HttpServletRequest req);

    public GMTResponseDTO approveLoan(String loanId, String approvedBy, HttpServletRequest req);

    public GMTResponseDTO rejectLoan(String loanId, String rejectedBy, String rejectionReason, HttpServletRequest req);

    public GMTResponseDTO getLoansByStatus(String status, String userId, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

    public GMTResponseDTO calculateLoanPayment(Double loanAmount, Double interestRate, Integer loanTerm);

    public GMTResponseDTO getActiveLoans(HttpServletRequest req) throws GMTCustomException;

    public GMTResponseDTO processLoanPayment(String loanId, Double paymentAmount, HttpServletRequest req) throws GMTCustomException;
}
