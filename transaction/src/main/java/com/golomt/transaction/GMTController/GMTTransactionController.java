package com.golomt.transaction.GMTController;

import com.golomt.transaction.GMTConstant.GMTLog;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTDepositRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTTransactionRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTInterBankTransactionRequestDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTErrorDTO;
import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.transaction.GMTException.*;
import com.golomt.transaction.GMTHelper.GMTResponse;
import com.golomt.transaction.GMTService.GMTTransactionService;
import com.golomt.transaction.GMTUtility.GMTLOGUtilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/transactions")

public class GMTTransactionController {

    private final GMTTransactionService gMTTransactionService;
    private GMTTransactionService transactionService;


    public GMTTransactionController(GMTTransactionService transactionService, GMTTransactionService gMTTransactionService) {
        this.transactionService = transactionService;
        this.gMTTransactionService = gMTTransactionService;
    }

    /**
     * Гүйлгээ хийх
     *
     * @param dto@{@link GMTTransactionRequestDTO}
     * @param req        @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE') ")
    @PostMapping("")
    public GMTResponseDTO processTransfer(@RequestBody GMTRequestDTO<GMTTransactionRequestDTO> dto, HttpServletRequest req) throws GMTRMIException, GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processTransfer][" + dto.getBody().getTransactionType().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");
            GMTResponseDTO response = transactionService.processTransfer(dto.getBody(), req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processTransfer][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processTransfer][" + dto.getBody().getTransactionType().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");
            return response;
        } catch (GMTRMIException e) {

            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction." + dto.getBody().getTransactionType().toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRuntimeException e) {

            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction." + dto.getBody().getTransactionType().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTValidationException | GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "validation.error")).getResponseDTO();
        }
    }

    /**
     * Банк хоорондын гүйлгээ хийх
     *
     * @param dto@{@link GMTInterBankTransactionRequestDTO}
     * @param req        @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE') ")
    @PostMapping("/inter-bank")
    public GMTResponseDTO processInterBankTransfer(@RequestBody GMTRequestDTO<GMTInterBankTransactionRequestDTO> dto, HttpServletRequest req) throws GMTRMIException, GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processInterBankTransfer][init][" + req.getRemoteUser() + "]");
            GMTResponseDTO response = transactionService.processInterBankTransfer(dto.getBody(), req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processInterBankTransfer][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processInterBankTransfer][end][" + req.getRemoteUser() + "]");
            return response;
        } catch (GMTRMIException e) {

            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.interBankTransfer][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRuntimeException e) {

            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.interBankTransfer][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTValidationException | GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processInterBankTransfer][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "validation.error")).getResponseDTO();
        }
    }

    /**
     * Банк хоорондын гүйлгээний төлөв шинэчлэх (гадаад банкнаас ирэх callback)
     *
     * @param transactionId Гүйлгээний ID
     * @param status Шинэ төлөв
     * @param externalReference Гадаад банкны лавлагаа
     * @param failureReason Алдааны шалтгаан
     * @param req HttpServletRequest
     * @return GMTResponseDTO
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_ADMIN') || hasRole('ROLE_BANK_OPERATOR')")
    @PutMapping("/inter-bank/{transactionId}/status")
    public GMTResponseDTO updateInterBankTransactionStatus(
            @PathVariable String transactionId,
            @RequestParam String status,
            @RequestParam(required = false) String externalReference,
            @RequestParam(required = false) String failureReason,
            HttpServletRequest req) throws GMTRMIException, GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.updateInterBankTransactionStatus][init][" + req.getRemoteUser() + "]");
            GMTResponseDTO response = transactionService.updateInterBankTransactionStatus(transactionId, status, externalReference, failureReason, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.updateInterBankTransactionStatus][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.updateInterBankTransactionStatus][end][" + req.getRemoteUser() + "]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.updateInterBankTransactionStatus][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.updateInterBankTransactionStatus][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "validation.error")).getResponseDTO();
        }
    }

    /**
     * Гүйлгээний баримт авах
     *
     * @param dto
     * @param req
     * @return @{@link GMTResponseDTO}
     */
//    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE') ")
//    @PostMapping("")
//    public GMTResponseDTO doTransactionReceipt(@RequestBody GMTRequestDTO<GMTTransactionReceiptRqDTO> dto, HttpServletRequest req) throws GMTRMIException, GMTBusinessException, GMTValidationException {
//        try {
//            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.receipt][" + dto.getBody().getTransactionId().toString().toLowerCase() + "][init][" + req.getRemoteUser() + "]");
//            GMTResponseDTO response = transactionService.doTransactionReceipt(dto.getBody(), req);
//
//            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.receipt][" + dto.getBody().getTransactionId().toString().toLowerCase() + "][end][" + req.getRemoteUser() + "]");
//            return response;
//        } catch (GMTBusinessException e) {
//
//            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.receipt][" + dto.getBody().getTransactionId().toString().toLowerCase() + "][business][" + req.getRemoteUser() + "] " + e.getMessage());
//            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO(null, e.getMessage(), GMTException.NOT_FOUNT.value())).getResponseDTO();
//
//        } catch (GMTRMIException e) {
//
//            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.receipt][" + dto.getBody().getTransactionId().toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
//            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
//
//        } catch (GMTRuntimeException e) {
//
//            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.receipt][" + dto.getBody().getTransactionId().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
//            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
//        } catch (Exception e) {
//
//            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[service][transaction.receipt][" + dto.getBody().getTransactionId().toString().toLowerCase() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
//            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
//
//        }
//    }

    /**
     * Гүйлгээний баримт авах
     *
     * @param dto
     * @param req
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @PostMapping("/deposit")
    public GMTResponseDTO processDeposit(@RequestBody GMTRequestDTO<GMTDepositRequestDTO> dto, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][init]" + req.getRemoteUser());
            GMTResponseDTO response = gMTTransactionService.processDeposit(dto.getBody(), req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][end]" + req.getRemoteUser());
            return response;
        } catch (GMTCustomException e) {
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "validation.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + dto.getBody().getDescription().toString().toLowerCase() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        }
    }

    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/transaction/{id}")
    public GMTResponseDTO getTransactionByTransactionId(@PathVariable String id, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + req.getRemoteUser() + "][" + id + "][init]");
            GMTResponseDTO response = gMTTransactionService.getTransactionByTransactionId(id, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + req.getRemoteUser() + "][" + id + "][end]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + id.toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + id.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByTransactionId][" + id.toString().toString() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        }
    }

    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping(value = "/receipt/{id}/pdf", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable String id, HttpServletRequest req) throws GMTCustomException {
        byte[] report = gMTTransactionService.generateTransactionReceiptPdf(id, req);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=receipt-" + id + ".pdf")
                .contentType(MediaType.TEXT_PLAIN)
                .body(report);
    }

    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping(value = "/receipts/user/{toUserId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadUserTransactionsPdf(@PathVariable String toUserId, HttpServletRequest req) throws GMTCustomException, GMTValidationException {
        byte[] pdf = gMTTransactionService.generateTransactionsListPdf(toUserId, req);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=transactions-" + toUserId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/user/{id}")
    public GMTResponseDTO getTransactionByUserId(@PathVariable String id, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + req.getRemoteUser() + "][init]");
            GMTResponseDTO response = gMTTransactionService.getAllTransactionsByUser(id, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + req.getRemoteUser() + "][end]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + id.toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + id.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + id.toString().toString() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        }
    }


    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/txn/user/{toUserId}")
    public GMTResponseDTO getTransactionByUser(@PathVariable String toUserId, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + req.getRemoteUser() + "][init]");
            GMTResponseDTO response = gMTTransactionService.getTransactionByRelatedUsers(toUserId, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + req.getRemoteUser() + "][end]");
            return response;
        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + toUserId.toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + toUserId.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTValidationException | GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + toUserId.toString().toString() + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "validation.error")).getResponseDTO();
        }
    }


    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/account/{accountNumber}")
    public GMTResponseDTO getTransactionsByAccountNumber(@PathVariable String accountNumber, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByAccountNumber][" + accountNumber + "][" + req.getRemoteUser() + "][init]");
            GMTResponseDTO response = gMTTransactionService.getTransactionsByAccountNumber(accountNumber, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByAccountNumber][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByAccountNumber][" + accountNumber + "][" + req.getRemoteUser() + "][init]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionByUserId][" + accountNumber + "][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + accountNumber + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + accountNumber + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.processDeposit][" + accountNumber + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "validation.error")).getResponseDTO();
        }
    }


    GMTResponseDTO getTransactionsByUserId(String id, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + id + "][init]");
            GMTResponseDTO response = gMTTransactionService.getTransactionByTransactionId(id, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + response.toString().toLowerCase() + "]");

            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + id + "][end]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + id.toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + id.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + id.toString().toString() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        }
    }

    GMTResponseDTO getUserTransactionsByUserId(String userId, HttpServletRequest req) throws GMTBusinessException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getUserTransactionsByUserId][" + userId + "][init]");
            GMTResponseDTO response = gMTTransactionService.getAllTransactionsByUser(userId, req);
            GMTLOGUtilities.debug(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + response.toString().toLowerCase() + "]");
            GMTLOGUtilities.info(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getUserTransactionsByUserId][" + userId + "][init]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_REQUEST.value(), null, new GMTErrorDTO("400", e.getMessage(), "system.error")).getResponseDTO();


        } catch (GMTRMIException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + userId.toString().toLowerCase() + "][rmi][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.BAD_GATEWAY.value(), null, new GMTErrorDTO("502", e.getMessage(), "system.error")).getResponseDTO();

        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + userId.toString().toLowerCase() + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();

        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.TRANSACTION.getValue(), "[controller][transactions.getTransactionsByUserId][" + userId.toString().toString() + "][unknown][" + req.getRemoteUser() + "] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, new GMTErrorDTO("500", e.getMessage(), "system.error")).getResponseDTO();
        }
    }

}