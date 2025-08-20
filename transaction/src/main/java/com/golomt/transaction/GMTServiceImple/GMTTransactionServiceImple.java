package com.golomt.transaction.GMTServiceImple;

import com.golomt.transaction.GMTConstant.GMTLog;
import com.golomt.transaction.GMTConstant.GMTTransactionStatus;
import com.golomt.transaction.GMTConstant.GMTTransactionType;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTDepositRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTTransactionReceiptRqDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTTransactionRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTWithdrawalRequestDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTTransactionDTO.GMTTransactionResponseDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTInterBankTransactionRequestDTO;
import com.golomt.transaction.GMTEntity.GMTTransactionEntity;
import com.golomt.transaction.GMTException.*;
import com.golomt.transaction.GMTHelper.GMTHelper;
import com.golomt.transaction.GMTHelper.GMTResponse;
import com.golomt.transaction.GMTRepository.GMTTransactionRepository;
import com.golomt.transaction.GMTService.GMTTransactionService;
import com.golomt.transaction.GMTUtility.GMTLOGUtilities;
import com.golomt.transaction.GMTUtility.GMTMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import static com.golomt.transaction.GMTUtility.GMTMapper.mapToResponse;


@Slf4j
@Transactional
@Service
public class GMTTransactionServiceImple implements GMTTransactionService {

    private final GMTTransactionRepository gmtTransactionRepository;

    @Inject
    public GMTTransactionServiceImple(GMTTransactionRepository gmtTransactionRepository) {
        this.gmtTransactionRepository = gmtTransactionRepository;
    }

    public GMTTransactionEntity createTransaction(GMTTransactionEntity transaction) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.createTransaction][" + transaction.getTransactionId().toLowerCase() + "][init]");

        if (gmtTransactionRepository.getTransactionByTransactionId(transaction.getTransactionId()) != null) {
            throw new GMTCustomException(transaction.getTransactionId() + " тай гүйлгээ аль хэдийн үүссэн байна. ");
        }

        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        if (transaction.getStatus() == null) {
            transaction.setStatus(String.valueOf(GMTTransactionStatus.PENDING));
        }

        if (transaction.getFee() == null) {
            transaction.setFee(0.0);
        }

        gmtTransactionRepository.save(transaction);
        GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[Амжилттай хадгалсан..][transaction: " + transaction + "]");

        GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.createTransaction][" + transaction.getId().toString().toLowerCase() + "][end]");

        return transaction;
    }

    public GMTResponseDTO processTransfer(GMTTransactionRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTSessionException, GMTRuntimeException, GMTRMIException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

            GMTTransactionEntity savedTransaction = processTransferExtension(dto, req);

            try {
                double fromBalance = GMTHelper.getAccountBalanceByAccountNumber(dto.getFromAccountNumber(), req);
                if (fromBalance - Math.abs(dto.getAmount()) < 0) {
                    savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                    savedTransaction.setFailureReason("Дансны үлдэгдэл хүрэлцэхгүй");
                    savedTransaction.setFee(0.0);
                    gmtTransactionRepository.save(savedTransaction);
                    GMTTransactionResponseDTO response = mapToResponse(savedTransaction);
                    return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Дансны үлдэгдэл хүрэлцэхгүй", response.getTransactionId(), response).getResponseDTO();
                }
            } catch (Exception balanceEx) {
                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                savedTransaction.setFailureReason(balanceEx.getMessage());
                savedTransaction.setFee(0.0);
                gmtTransactionRepository.save(savedTransaction);
                GMTTransactionResponseDTO response = mapToResponse(savedTransaction);
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), balanceEx.getMessage(), response.getTransactionId(), response).getResponseDTO();
            }

            Long fromId = GMTHelper.resolveAccountIdByAccountNumber(dto.getFromAccountNumber(), req);
            boolean fromAccountStatus = GMTHelper.resolveAccountActiveByAccountNumber(dto.getFromAccountNumber(), req);
            if (!fromAccountStatus) {
                throw new GMTCustomException("Шилжүүлэх данс идэвхгүй байна");
            }
            GMTHelper.adjustAccountBalanceById(fromId, -Math.abs(dto.getAmount() + GMTHelper.calculateTransferFee(dto.getAmount())), req);

            try {
                Long toId = GMTHelper.resolveAccountIdByAccountNumber(dto.getToAccountNumber(), req);
                boolean toAccountStatus = GMTHelper.resolveAccountActiveByAccountNumber(dto.getToAccountNumber(), req);
                if (!toAccountStatus) {
                    throw new GMTCustomException("Шилжүүлэх данс идэвхгүй байна");
                }
                GMTHelper.adjustAccountBalanceById(toId, Math.abs(dto.getAmount()), req);
            } catch (Exception creditEx) {
                try {
                    GMTHelper.adjustAccountBalanceById(fromId, Math.abs(dto.getAmount()), req);
                } catch (Exception ignored) {
                }
                throw creditEx;
            }

            savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.COMPLETED));
            savedTransaction.setProcessedAt(java.time.LocalDateTime.now());
            gmtTransactionRepository.save(savedTransaction);

            GMTTransactionResponseDTO response = mapToResponse(savedTransaction);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[Амжилттай response ирлээ..][response: " + response + "]");
            return new GMTResponse(HttpStatus.OK.value(), "", response.getTransactionId(), response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][unexpected][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO processInterBankTransfer(GMTInterBankTransactionRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTSessionException, GMTRuntimeException, GMTRMIException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][init][" + req.getRemoteUser() + "]");

            GMTTransactionEntity savedTransaction = processInterBankTransferExtension(dto, req);

            // For inter-bank transfers, we need to check if the destination is actually external
            if (!GMTHelper.isInterBankAccount(dto.getToAccountNumber())) {
                throw new GMTCustomException("Хүлээн авах данс нь гадаад банкны данс биш байна. Энэ нь дотоод данс байна: " + dto.getToAccountNumber());
            }

            try {
                double fromBalance = GMTHelper.getAccountBalanceByAccountNumber(dto.getFromAccountNumber(), req);
                Double totalAmount = dto.getAmount() + savedTransaction.getFee();
                if (fromBalance - Math.abs(totalAmount) < 0) {
                    savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                    savedTransaction.setFailureReason("Дансны үлдэгдэл хүрэлцэхгүй");
                    savedTransaction.setFee(0.0);
                    gmtTransactionRepository.save(savedTransaction);
                    GMTTransactionResponseDTO response = mapToResponse(savedTransaction);
                    return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Дансны үлдэгдэл хүрэлцэхгүй", response.getTransactionId(), response).getResponseDTO();
                }
            } catch (Exception balanceEx) {
                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                savedTransaction.setFailureReason(balanceEx.getMessage());
                savedTransaction.setFee(0.0);
                gmtTransactionRepository.save(savedTransaction);
                GMTTransactionResponseDTO response = mapToResponse(savedTransaction);
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), balanceEx.getMessage(), response.getTransactionId(), response).getResponseDTO();
            }

            Long fromId = GMTHelper.resolveAccountIdByAccountNumber(dto.getFromAccountNumber(), req);
            boolean fromAccountStatus = GMTHelper.resolveAccountActiveByAccountNumber(dto.getFromAccountNumber(), req);
            if (!fromAccountStatus) {
                throw new GMTCustomException("Шилжүүлэх данс идэвхгүй байна");
            }

            GMTHelper.adjustAccountBalanceById(fromId, -Math.abs(dto.getAmount() + savedTransaction.getFee()), req);

            savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.PENDING));
            savedTransaction.setProcessedAt(java.time.LocalDateTime.now());
            gmtTransactionRepository.save(savedTransaction);

            
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][SWIFT_SENT][" + req.getRemoteUser() + "]");

            GMTTransactionResponseDTO response = mapToResponse(savedTransaction);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[Амжилттай response ирлээ..][response: " + response + "]");
            
            return new GMTResponse(HttpStatus.OK.value(), "Банк хоорондын гүйлгээ амжилттай илгээгдлээ", response.getTransactionId(), response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransfer][unexpected][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTTransactionEntity processInterBankTransferExtension(GMTInterBankTransactionRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransferExtension][init][" + req.getRemoteUser() + "]");

            GMTHelper.validateInterBankTransactionReq(dto);

            GMTTransactionEntity transaction = new GMTTransactionEntity();
            transaction.setTransactionId(GMTHelper.generateTransactionId());
            transaction.setFromAccountNumber(dto.getFromAccountNumber());
            transaction.setToAccountNumber(dto.getToAccountNumber());
            transaction.setAmount(dto.getAmount());
            transaction.setCurrencyCode(dto.getCurrencyCode());
            transaction.setTransactionType(String.valueOf(GMTTransactionType.INTER_BANK_TRANSFER));
            transaction.setDescription(dto.getDescription());
            transaction.setCreatedBy(req.getRemoteUser());
            transaction.setToUserId(dto.getToUserId());

            transaction.setStatus(String.valueOf(GMTTransactionStatus.PENDING));
            transaction.setFee(GMTHelper.calculateInterBankTransferFee(dto.getAmount(), dto.getCurrencyCode()));
            
            if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                transaction.setDescription(transaction.getDescription());
            }

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransferExtension][end][" + req.getRemoteUser() + "]");

            return createTransaction(transaction);
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransferExtension][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            GMTTransactionEntity failed = new GMTTransactionEntity();
            failed.setTransactionId(GMTHelper.generateTransactionId());
            failed.setFromAccountNumber(dto.getFromAccountNumber());
            failed.setToAccountNumber(dto.getToAccountNumber());
            failed.setAmount(dto.getAmount());
            failed.setCurrencyCode(dto.getCurrencyCode());
            failed.setTransactionType(String.valueOf(GMTTransactionType.INTER_BANK_TRANSFER));
            failed.setDescription(dto.getDescription());
            failed.setCreatedBy(req.getRemoteUser());
            failed.setToUserId(dto.getToUserId());
            failed.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
            failed.setFailureReason(e.getMessage());
            failed.setFee(0.0);
            gmtTransactionRepository.save(failed);
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransferExtension][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransferExtension][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processInterBankTransferExtension][unexpected][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTTransactionEntity processTransferExtension(GMTTransactionRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransferExtension][" + dto.getTransactionType().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

            GMTHelper.validateTransactionReq(dto);

            GMTTransactionEntity transaction = new GMTTransactionEntity();
            transaction.setTransactionId(GMTHelper.generateTransactionId());
            transaction.setFromAccountNumber(dto.getFromAccountNumber());
            transaction.setToAccountNumber(dto.getToAccountNumber());
            transaction.setAmount(dto.getAmount());
            transaction.setCurrencyCode(dto.getCurrencyCode());
            transaction.setTransactionType(String.valueOf(GMTTransactionType.TRANSFER));
            transaction.setDescription(dto.getDescription());
            transaction.setCreatedBy(req.getRemoteUser());
            transaction.setToUserId(dto.getToUserId());

            transaction.setStatus(String.valueOf(GMTTransactionStatus.PENDING));
            transaction.setFee(GMTHelper.calculateTransferFee(dto.getAmount()));
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransferExtension][" + dto.getTransactionType().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");

            return createTransaction(transaction);
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransferExtension][" + dto.getTransactionType().toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            GMTTransactionEntity failed = new GMTTransactionEntity();
            failed.setTransactionId(GMTHelper.generateTransactionId());
            failed.setFromAccountNumber(dto.getFromAccountNumber());
            failed.setToAccountNumber(dto.getToAccountNumber());
            failed.setAmount(dto.getAmount());
            failed.setCurrencyCode(dto.getCurrencyCode());
            failed.setTransactionType(String.valueOf(GMTTransactionType.TRANSFER));
            failed.setDescription(dto.getDescription());
            failed.setCreatedBy(req.getRemoteUser());
            failed.setToUserId(dto.getToUserId());
            failed.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
            failed.setFailureReason(e.getMessage());
            failed.setFee(0.0);
            gmtTransactionRepository.save(failed);
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransferExtension][" + dto.getTransactionType().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getTransactionType().toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransferExtension][" + dto.getTransactionType().toString().toLowerCase() + "][unexpected][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }


    public GMTResponseDTO processDeposit(GMTDepositRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException, GMTRuntimeException {

        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getDescription().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

            GMTTransactionEntity savedTransaction = processDepositExtension(dto, req);

            try {
                Long toId = GMTHelper.resolveAccountIdByAccountNumber(dto.getAccountNumber(), req);
                GMTHelper.adjustAccountBalanceById(toId, Math.abs(dto.getAmount()), req);

                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.COMPLETED));
                savedTransaction.setProcessedAt(LocalDateTime.now());
                savedTransaction = gmtTransactionRepository.save(savedTransaction);
            } catch (Exception adjustEx) {
                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                savedTransaction.setFailureReason(adjustEx.getMessage());
                savedTransaction.setFee(0.0);
                gmtTransactionRepository.save(savedTransaction);
                throw adjustEx;
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[Амжилттай хадгалсан..][transaction: " + savedTransaction + "]");

            GMTTransactionResponseDTO response = mapToResponse(savedTransaction);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processTransfer][" + dto.getDescription().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай цэнэглэлээ", response.getTransactionId(), response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTTransactionEntity processDepositExtension(GMTDepositRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");
            GMTHelper.validateTransactionReq(dto);

            GMTTransactionEntity transaction = new GMTTransactionEntity();
            transaction.setTransactionId(GMTHelper.generateTransactionId());
            transaction.setToAccountNumber(dto.getAccountNumber());
            transaction.setFromAccountNumber("EXTERNAL");
            transaction.setAmount(dto.getAmount());
            transaction.setCurrencyCode(dto.getCurrencyCode());
            transaction.setTransactionType(GMTTransactionType.DEPOSIT.toString());
            transaction.setDescription(dto.getDescription());
            transaction.setCreatedBy(req.getRemoteUser());
            transaction.setStatus(GMTTransactionStatus.PENDING.toString());
            transaction.setFee(0.0);
            transaction.setToUserId(dto.getToUserId());
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + transaction + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + transaction.getId() + "][end][" + req.getRemoteUser() + "]");

            return createTransaction(transaction);
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getDescription().toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }

    public GMTResponseDTO processWithdrawal(GMTWithdrawalRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processWithdrawal][" + dto.getAccountNumber().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");
            GMTTransactionEntity savedTransaction = processWithdrawalExtension(dto, req);

            // Guard: account balance must not go below zero
            try {
                double fromBalance = GMTHelper.getAccountBalanceByAccountNumber(dto.getAccountNumber(), req);
                if (fromBalance - Math.abs(dto.getAmount()) < 0) {
                    savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                    savedTransaction.setFailureReason("Дансны үлдэгдэл хүрэлцэхгүй");
                    savedTransaction = gmtTransactionRepository.save(savedTransaction);
                    savedTransaction.setFee(0.0);
                    GMTTransactionResponseDTO response = mapToResponse(savedTransaction);
                    return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Дансны үлдэгдэл хүрэлцэхгүй", response.getTransactionId(), response).getResponseDTO();
                }
            } catch (Exception balanceEx) {
                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                savedTransaction.setFee(0.0);
                savedTransaction.setFailureReason(balanceEx.getMessage());
                savedTransaction = gmtTransactionRepository.save(savedTransaction);
                GMTTransactionResponseDTO response = mapToResponse(savedTransaction);
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), balanceEx.getMessage(), response.getTransactionId(), response).getResponseDTO();
            }

            try {
                Long fromId = GMTHelper.resolveAccountIdByAccountNumber(dto.getAccountNumber(), req);
                GMTHelper.adjustAccountBalanceById(fromId, -Math.abs(dto.getAmount()), req);

                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.COMPLETED));
                savedTransaction.setProcessedAt(LocalDateTime.now());
                savedTransaction = gmtTransactionRepository.save(savedTransaction);
            } catch (Exception adjustEx) {
                savedTransaction.setStatus(String.valueOf(GMTTransactionStatus.FAILED));
                savedTransaction.setFailureReason(adjustEx.getMessage());
                gmtTransactionRepository.save(savedTransaction);
                savedTransaction.setFee(0.0);
                throw adjustEx;
            }

            GMTTransactionResponseDTO response = mapToResponse(savedTransaction);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processWithdrawal][" + dto.getAccountNumber().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай боловсруулагдлаа", response.getTransactionId(), response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }


    public GMTTransactionEntity processWithdrawalExtension(GMTWithdrawalRequestDTO dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processWithdrawalExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");

            GMTHelper.validateTransactionReq(dto);

            GMTTransactionEntity transaction = new GMTTransactionEntity();
            transaction.setTransactionId(GMTHelper.generateTransactionId());
            transaction.setFromAccountNumber(dto.getAccountNumber());
            transaction.setToAccountNumber("EXTERNAL");
            transaction.setAmount(dto.getAmount());
            transaction.setCurrencyCode(dto.getCurrencyCode());
            transaction.setTransactionType(GMTTransactionType.WITHDRAWAL.toString());
            transaction.setDescription(dto.getDescription());
            transaction.setCreatedBy(req.getRemoteUser());
            transaction.setStatus(String.valueOf(GMTTransactionStatus.PENDING));
            transaction.setFee(GMTHelper.calculateWithdrawalFee(dto.getAmount()));
            transaction.setToUserId(dto.getToUserId());

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.processWithdrawalExtension][" + transaction + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.processWithdrawalExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");

            return createTransaction(transaction);
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + dto.getAccountNumber().toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTransactionByTransactionId(String id, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException {
        GMTTransactionResponseDTO response = null;
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getByTransactionId][" + id + "][init][" + req.getRemoteUser() + "]");

            if (gmtTransactionRepository.getTransactionByTransactionId(id) != null) {
                GMTTransactionEntity transaction = gmtTransactionRepository.findByTransactionId(id);
                GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "response: " + transaction);
                response = mapToResponse(transaction);
                GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getByTransactionId][" + id + "][debug][" + req.getRemoteUser() + "]");
                GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getByTransactionId][" + id + "][end][" + req.getRemoteUser() + "]");

                return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response.getTransactionId(), response).getResponseDTO();

            } else {
                throw new GMTCustomException(id + " тай гүйлгээ аль олдохгүй байна. ");
            }
        } catch (GMTRuntimeException | GMTCustomException e) {
            throw e;
        }

    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTransactionByRelatedUsers(String toUserId, HttpServletRequest req) throws GMTRuntimeException, GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsers][" + toUserId + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = getTransactionByRelatedUsersExtension(toUserId, req);
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsers][" + toUserId + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[Амжилттай ирлээ..][response: " + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsers][" + toUserId + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsers][" + toUserId + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsers][" + toUserId + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }

    public List<GMTTransactionEntity> getTransactionByRelatedUsersExtension(String toUserId, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsersExtension][" + toUserId + "][end][" + req.getRemoteUser() + "]");
            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByRelatedUsers(toUserId, req.getRemoteUser());
            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй");
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай олдлоо...][transaction :" + transactions + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsersExtension][" + transactions + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsersExtension][" + toUserId + "][end][" + req.getRemoteUser() + "]");
            return transactions;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsersExtension][" + toUserId + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionByUsersExtension][" + toUserId + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAllTransactionsByUser(String userId, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUser][" + userId + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = getAllTransactionsByUserExtension(userId, req);
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUser][" + userId + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUser][" + userId.toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUser][" + userId.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUser][" + userId.toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }

    public List<GMTTransactionEntity> getAllTransactionsByUserExtension(String userId, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUserExtension][" + userId + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByUser(userId);
            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй");
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUserExtension][" + userId + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай олдлоо...][transaction :" + transactions + "]");

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getAllTransactionsByUserExtension][" + userId + "][end][" + req.getRemoteUser() + "]");
            return transactions;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + userId + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.processDepositExtension][" + userId + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTransactionsByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumber][" + accountNumber + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = getTransactionsByAccountNumberExtension(accountNumber, req);
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumber][" + accountNumber + "][end][" + req.getRemoteUser() + "]");
            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumber][" + accountNumber + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }

    public List<GMTTransactionEntity> getTransactionsByAccountNumberExtension(String accountNumber, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumberExtension][" + accountNumber + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getAllTransactions(accountNumber);
            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй");
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай олдлоо...][transaction :" + transactions + "]");

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumberExtension][" + accountNumber + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumberExtension][" + accountNumber + "][end][" + req.getRemoteUser() + "]");
            return transactions;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumberExtension][" + accountNumber + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountNumberExtension][" + accountNumber + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getUserTransactionsByAccountNumber(String accountNumber, String userId, HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumber][" + accountNumber + "][init][" + req.getRemoteUser() + "]");
            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByAccountNumber(userId, accountNumber);
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumber][" + accountNumber + "][end][" + req.getRemoteUser() + "]");
            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumber][" + accountNumber + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }

    public List<GMTTransactionEntity> getUserTransactionsByAccountNumberExtension(String userId, String accountNumber, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + accountNumber + "][init][" + req.getRemoteUser() + "]");
            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByAccountNumber(userId, accountNumber);

            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй");
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай олдлоо...][transaction :" + transactions + "]");

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + accountNumber + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + accountNumber + "][end][" + req.getRemoteUser() + "]");
            return transactions;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + accountNumber + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + accountNumber + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTransactionsByStatus(String status, String userId, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatus][" + status.toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = getTransactionsByStatusExtension(status, userId, req);
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatus][" + status.toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");
            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatus][" + status.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatus][" + status.toString().toLowerCase() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatus][" + status.toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public List<GMTTransactionEntity> getTransactionsByStatusExtension(String status, String userId, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + status.toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByStatus(userId, status.toString());
            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй");
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай олдлоо...][transaction :" + transactions + "]");

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + status.toString().toLowerCase() + "][debug][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getUserTransactionsByAccountNumberExtension][" + status.toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");

            return transactions;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatusExtension][" + status.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByStatusExtension][" + status.toString().toLowerCase() + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRange][" + startDate + "][" + endDate + "][init][" + req.getRemoteUser() + "]");
            List<GMTTransactionEntity> transactions = getTransactionsByDateRangeExtension(startDate, endDate, req.getRemoteUser());
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRange][" + startDate + "][" + endDate + "][end][" + req.getRemoteUser() + "]");
            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRange][" + startDate + "][" + endDate + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRange][" + startDate + "][" + endDate + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }

    }

    public List<GMTTransactionEntity> getTransactionsByDateRangeExtension(LocalDateTime startDate, LocalDateTime endDate, String userId) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRangeExtension][" + startDate + "][" + endDate + "][init][" + userId + "]");
            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByDateRange(userId, startDate, endDate);
            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй");
            }
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай олдлоо...][transaction :" + transactions + "]");

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRangeExtension][" + startDate + "][" + endDate + "][init][" + userId + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRangeExtension][" + startDate + "][" + endDate + "][init][" + userId + "]");
            return transactions;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRangeExtension][" + startDate + "][" + endDate + "][runtime][" + userId + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByDateRangeExtension][" + startDate + "][" + endDate + "][custom][" + userId + "] " + e.getMessage());
            throw e;
        }
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTransactionsByAccountAndStatus(String accountNumber, String status, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountAndStatus][" + accountNumber + "][" + status + "][init][" + req.getRemoteUser() + "]");
        List<GMTTransactionEntity> transactions = gmtTransactionRepository.findByFromAccountNumberAndStatus(accountNumber, status);
        List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);
        GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "response: " + transactions);
        GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[амжилттай ирлээ...][response :" + transactions + "]");

        GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.getTransactionsByAccountAndStatus][" + accountNumber + "][" + status + "][end][" + req.getRemoteUser() + "]");
        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
    }

    public GMTResponseDTO updateTransactionStatus(String transactionId, String status, String updatedBy, String failureReason, HttpServletRequest req) {
        log.info("Updating transaction status: {} to {}", transactionId, status);
        GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateTransactionStatus][" + transactionId + status.toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

        GMTTransactionEntity transaction = gmtTransactionRepository.getTransactionByTransactionId(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("Гүйлгээний ID олсонгүй: " + transactionId);
        }

        transaction.setStatus(status);
        transaction.setUpdatedBy(updatedBy);
        transaction.setUpdatedAt(LocalDateTime.now());

        if (status == GMTTransactionStatus.COMPLETED.toString()) {
            transaction.setProcessedAt(LocalDateTime.now());
        }

        if (status == GMTTransactionStatus.FAILED.toString() && failureReason != null) {
            transaction.setFailureReason(failureReason);
        }

        GMTTransactionEntity updatedTransaction = gmtTransactionRepository.save(transaction);
        GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "response: " + updatedTransaction);

        GMTTransactionResponseDTO response = GMTMapper.mapToResponse(updatedTransaction);
        GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateTransactionStatus][" + transactionId + status.toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");
        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай солигдлоо...", response).getResponseDTO();
    }

    public GMTResponseDTO processTransaction(Long transactionId, String processedBy) {
        Optional<GMTTransactionEntity> optionalTransaction = gmtTransactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            throw new IllegalArgumentException("Гүйлгээний ID олсонгүй: " + transactionId);
        }

        GMTTransactionEntity transaction = optionalTransaction.get();

        if (!GMTTransactionStatus.PENDING.name().equals(transaction.getStatus())) {
            throw new IllegalStateException("Зөвхөн хүлээгдэж буй гүй Амжилттай болно");
        }

        transaction.setStatus(String.valueOf(GMTTransactionStatus.COMPLETED));
        transaction.setUpdatedBy(processedBy);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setProcessedAt(LocalDateTime.now());

        GMTTransactionEntity processedTransaction = gmtTransactionRepository.save(transaction);
        GMTTransactionResponseDTO response = GMTMapper.mapToResponse(processedTransaction);
        GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "response: " + processedTransaction);

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай боловсруулагдлаа...", response).getResponseDTO();
    }

    public GMTResponseDTO cancelTransaction(Long transactionId, String cancelledBy, String reason, HttpServletRequest req) {

        Optional<GMTTransactionEntity> optionalTransaction = gmtTransactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            throw new IllegalArgumentException("Гүйлгээний ID олсонгүй: " + transactionId);
        }

        GMTTransactionEntity transaction = optionalTransaction.get();

        if (GMTTransactionStatus.COMPLETED.name().equals(transaction.getStatus())) {
            throw new IllegalStateException("Амжилттай шилжүүлэгдсэн гүйлгээ цуцлах боломжгүй");
        }
        transaction.setStatus(GMTTransactionStatus.CANCELLED.name());
        transaction.setUpdatedBy(cancelledBy);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setFailureReason(reason);

        gmtTransactionRepository.save(transaction);
        GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "response: " + transaction);

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай устлаа>...", transaction).getResponseDTO();
    }


    public GMTResponseDTO getTransactionsByAmountRange(Double minAmount, Double maxAmount, HttpServletRequest req) {
        try {
            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getTransactionsByAmountRange(minAmount, maxAmount);
            List<GMTTransactionResponseDTO> response = GMTMapper.mapToResponseList(transactions);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "response: " + transactions);

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо.", response).getResponseDTO();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public byte[] generateTransactionReceiptPdf(String transactionId, HttpServletRequest req) throws GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionReceiptPdf][" + transactionId + "][init]");

            GMTTransactionEntity tx = gmtTransactionRepository.findByTransactionId(transactionId);
            if (tx == null) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй: " + transactionId);
            }

            byte[] bytes = GMTHelper.exportReport(tx, req.getRemoteUser());

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionReceiptPdf][Гүйлгээний баримт амжилттай үүслээ]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionReceiptPdf][" + transactionId + "][end]");

            return bytes;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionReceiptPdf][" + transactionId + "][Алдаа: " + e.getMessage() + "]");
            throw new GMTCustomException("Гүйлгээний баримт үүсгэхэд алдаа гарлаа: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateTransactionsListPdf(String toUserId, HttpServletRequest req) throws GMTCustomException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionsListPdf][" + toUserId + "][init]");

            String currentUser = req.getRemoteUser();
            if (currentUser == null) {
                throw new GMTCustomException("Хэрэглэгч тодорхойгүй байна");
            }

            List<GMTTransactionEntity> transactions = gmtTransactionRepository.getUserTransactionsByRelatedUsers(toUserId, currentUser);
            if (transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээний түүх олдсонгүй: " + toUserId);
            }

            byte[] bytes = GMTHelper.exportReport(transactions, currentUser);

            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionsListPdf][Гүйлгээний жагсаалт амжилттай үүслээ]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionsListPdf][" + toUserId + "][end]");

            return bytes;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.generateTransactionsListPdf][" + toUserId + "][Алдаа: " + e.getMessage() + "]");
            throw new GMTCustomException("Гүйлгээний жагсаалт үүсгэхэд алдаа гарлаа: " + e.getMessage());
        }
    }


    public GMTResponseDTO updateInterBankTransactionStatus(String transactionId, String status, String externalReference, String failureReason, HttpServletRequest req) throws GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][init][" + req.getRemoteUser() + "]");

            GMTTransactionEntity transaction = gmtTransactionRepository.getTransactionByTransactionId(transactionId);
            if (transaction == null) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй: " + transactionId);
            }

            if (!GMTTransactionType.INTER_BANK_TRANSFER.equals(transaction.getTransactionType())) {
                throw new GMTCustomException("Энэ нь банк хоорондын гүйлгээ биш байна: " + transactionId);
            }

            String oldStatus = transaction.getStatus();
            transaction.setStatus(status);
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction.setUpdatedBy(req.getRemoteUser());

            if (String.valueOf(GMTTransactionStatus.COMPLETED).equals(status)) {
                transaction.setProcessedAt(LocalDateTime.now());
                if (externalReference != null) {
                    transaction.setDescription(transaction.getDescription() + " | External Ref: " + externalReference);
                }
                GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][COMPLETED][" + req.getRemoteUser() + "]");
                
            } else if (String.valueOf(GMTTransactionStatus.FAILED).equals(status)) {
                transaction.setFailureReason(failureReason != null ? failureReason : "External bank failed the transaction");
                transaction.setFee(0.0); // Refund the fee
                
                // Refund the sender account
                try {
                    Long fromId = GMTHelper.resolveAccountIdByAccountNumber(transaction.getFromAccountNumber(), req);
                    GMTHelper.adjustAccountBalanceById(fromId, Math.abs(transaction.getAmount() + transaction.getFee()), req);
                    GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][REFUNDED][" + req.getRemoteUser() + "]");
                } catch (Exception refundEx) {
                    GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][REFUND_FAILED][" + req.getRemoteUser() + "] " + refundEx.getMessage());
                    // Don't throw here, as the main transaction update should still succeed
                }
                
            } else if (String.valueOf(GMTTransactionStatus.CANCELLED).equals(status)) {
                transaction.setFailureReason(failureReason != null ? failureReason : "Transaction cancelled by external bank");
                transaction.setFee(0.0); // Refund the fee
                
                // Refund the sender account
                try {
                    Long fromId = GMTHelper.resolveAccountIdByAccountNumber(transaction.getFromAccountNumber(), req);
                    GMTHelper.adjustAccountBalanceById(fromId, Math.abs(transaction.getAmount() + transaction.getFee()), req);
                    GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][REFUNDED][" + req.getRemoteUser() + "]");
                } catch (Exception refundEx) {
                    GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][REFUND_FAILED][" + req.getRemoteUser() + "] " + refundEx.getMessage());
                }
            }

            gmtTransactionRepository.save(transaction);

            GMTTransactionResponseDTO response = mapToResponse(transaction);

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[Амжилттай шинэчлэгдлээ..][response: " + response + "]");

            String message = String.format("Гүйлгээний төлөв %s-с %s болж шинэчлэгдлээ", oldStatus, status);
            return new GMTResponse(HttpStatus.OK.value(), message, response.getTransactionId(), response).getResponseDTO();

        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][unexpected][" + req.getRemoteUser() + "] " + e.getMessage());
            throw new GMTCustomException("Гүйлгээний төлөв шинэчлэхэд алдаа гарсан: " + e.getMessage());
        }
    }

}





