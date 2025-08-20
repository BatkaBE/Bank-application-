package com.golomt.transaction.GMTService;

import com.golomt.transaction.GMTConstant.GMTTransactionStatus;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTDepositRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTTransactionReceiptRqDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTTransactionRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTWithdrawalRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTInterBankTransactionRequestDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTTransactionDTO.GMTTransactionReceiptRsDTO;
import com.golomt.transaction.GMTEntity.GMTTransactionEntity;
import com.golomt.transaction.GMTException.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


public interface GMTTransactionService {

    public GMTTransactionEntity createTransaction(GMTTransactionEntity transaction) throws GMTCustomException;

    public GMTResponseDTO processDeposit(GMTDepositRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException, GMTRuntimeException;

    public GMTResponseDTO getTransactionByTransactionId(String id, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException;

    public GMTResponseDTO getAllTransactionsByUser(String userId, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException, GMTValidationException;

    public GMTResponseDTO getTransactionsByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

    public GMTResponseDTO getUserTransactionsByAccountNumber(String accountNumber,String userId, HttpServletRequest req);

    public GMTResponseDTO getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

    public GMTResponseDTO getTransactionsByAmountRange(Double minAmount, Double maxAmount, HttpServletRequest req);

    public GMTResponseDTO updateTransactionStatus(String transactionId, String status, String updatedBy, String failureReason, HttpServletRequest req);

    public GMTResponseDTO cancelTransaction(Long transactionId, String cancelledBy, String reason, HttpServletRequest req);

    public GMTResponseDTO getTransactionsByStatus(String status, String userId, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

    public GMTResponseDTO processTransfer(GMTTransactionRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTSessionException, GMTRuntimeException, GMTRMIException, GMTCustomException;

    public GMTResponseDTO processInterBankTransfer(GMTInterBankTransactionRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTSessionException, GMTRuntimeException, GMTRMIException, GMTCustomException;

    public GMTResponseDTO updateInterBankTransactionStatus(String transactionId, String status, String externalReference, String failureReason, HttpServletRequest req) throws GMTCustomException;

    public GMTResponseDTO getTransactionByRelatedUsers(String toUserId, HttpServletRequest req) throws GMTValidationException, GMTCustomException;

	public byte[] generateTransactionReceiptPdf(String transactionId, HttpServletRequest req) throws GMTCustomException;

	public byte[] generateTransactionsListPdf(String toUserId, HttpServletRequest req) throws GMTCustomException, GMTValidationException;
}