package com.golomt.loan.GMTController;

import com.golomt.loan.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.loan.GMTDTO.GMTRequestDTO.GMTLoanDTO.GMTLoanRequestDTO;
import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.loan.GMTService.GMTLoanService;
import com.golomt.loan.GMTUtility.GMTLOGUtilities;
import com.golomt.loan.GMTConstant.GMTLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/loans")
@Slf4j
public class GMTLoanController {

    private final GMTLoanService gmtLoanService;

    public GMTLoanController(GMTLoanService gmtLoanService) {
        this.gmtLoanService = gmtLoanService;
    }

    /**
     * Зээл хүсэх
     *
     * @param dto @{@link GMTRequestDTO}
     * @param req @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @PostMapping("")
    public ResponseEntity<GMTResponseDTO> applyForLoan(@Valid @RequestBody GMTRequestDTO<GMTLoanRequestDTO> dto, HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.applyForLoan][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.applyForLoan(dto, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.applyForLoan][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.applyForLoan][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Зээлийн мэдээлэл авах
     *
     * @param loanId зээлийн ID
     * @param req    @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/{loanId}")
    public ResponseEntity<GMTResponseDTO> getLoanByLoanId(@PathVariable String loanId, HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoanByLoanId][" + loanId + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getLoanByLoanId(loanId, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoanByLoanId][" + loanId + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getLoanByLoanId][" + loanId + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Хэрэглэгчийн бүх зээл авах
     *
     * @param userId хэрэглэгчийн ID
     * @param req    @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<GMTResponseDTO> getAllLoansByUser(@PathVariable String userId, HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getAllLoansByUser][" + userId + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getAllLoansByUser(userId, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getAllLoansByUser][" + userId + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getAllLoansByUser][" + userId + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Дансны дугаараар зээл хайх
     *
     * @param accountNumber дансны дугаар
     * @param req          @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<GMTResponseDTO> getLoansByAccountNumber(@PathVariable String accountNumber, HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByAccountNumber][" + accountNumber + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getLoansByAccountNumber(accountNumber, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByAccountNumber][" + accountNumber + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByAccountNumber][" + accountNumber + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Огнооны хязгаараар зээл хайх
     *
     * @param startDate эхлэх огноо
     * @param endDate   дуусах огноо
     * @param req       @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/date-range")
    public ResponseEntity<GMTResponseDTO> getLoansByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByDateRange][" + startDate + " to " + endDate + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getLoansByDateRange(startDate, endDate, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByDateRange][" + startDate + " to " + endDate + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByDateRange][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Хэмжээний хязгаараар зээл хайх
     *
     * @param minAmount хамгийн бага хэмжээ
     * @param maxAmount хамгийн их хэмжээ
     * @param req       @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/amount-range")
    public ResponseEntity<GMTResponseDTO> getLoansByAmountRange(
            @RequestParam Double minAmount,
            @RequestParam Double maxAmount,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByAmountRange][" + minAmount + " to " + maxAmount + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getLoansByAmountRange(minAmount, maxAmount, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByAmountRange][" + minAmount + " to " + maxAmount + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByAmountRange][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Зээлийн статус шинэчлэх
     *
     * @param loanId         зээлийн ID
     * @param status         шинэ статус
     * @param updatedBy      шинэчилсэн хүн
     * @param rejectionReason татгалзах шалтгаан
     * @param req            @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_LOAN_OFFICER')")
    @PutMapping("/{loanId}/status")
    public ResponseEntity<GMTResponseDTO> updateLoanStatus(
            @PathVariable String loanId,
            @RequestParam String status,
            @RequestParam String updatedBy,
            @RequestParam(required = false) String rejectionReason,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.updateLoanStatus][" + loanId + " " + status + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.updateLoanStatus(loanId, status, updatedBy, rejectionReason, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.updateLoanStatus][" + loanId + " " + status + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.updateLoanStatus][" + loanId + " " + status + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Зээл зөвшөөрөх
     *
     * @param loanId     зээлийн ID
     * @param approvedBy зөвшөөрсөн хүн
     * @param req        @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_LOAN_OFFICER')")
    @PutMapping("/{loanId}/approve")
    public ResponseEntity<GMTResponseDTO> approveLoan(
            @PathVariable String loanId,
            @RequestParam String approvedBy,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.approveLoan][" + loanId + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.approveLoan(loanId, approvedBy, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.approveLoan][" + loanId + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.approveLoan][" + loanId + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Зээл татгалзах
     *
     * @param loanId         зээлийн ID
     * @param rejectedBy     татгалзсан хүн
     * @param rejectionReason татгалзах шалтгаан
     * @param req            @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_LOAN_OFFICER')")
    @PutMapping("/{loanId}/reject")
    public ResponseEntity<GMTResponseDTO> rejectLoan(
            @PathVariable String loanId,
            @RequestParam String rejectedBy,
            @RequestParam String rejectionReason,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.rejectLoan][" + loanId + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.rejectLoan(loanId, rejectedBy, rejectionReason, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.rejectLoan][" + loanId + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.rejectLoan][" + loanId + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Статусаар зээл хайх
     *
     * @param status статус
     * @param userId хэрэглэгчийн ID
     * @param req    @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @GetMapping("/status/{status}/user/{userId}")
    public ResponseEntity<GMTResponseDTO> getLoansByStatus(
            @PathVariable String status,
            @PathVariable String userId,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByStatus][" + status + "][" + userId + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getLoansByStatus(status, userId, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByStatus][" + status + "][" + userId + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getLoansByStatus][" + status + "][" + userId + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Зээлийн төлбөр тооцоолох
     *
     * @param loanAmount   зээлийн хэмжээ
     * @param interestRate хүүгийн түвшин
     * @param loanTerm     зээлийн хугацаа
     * @return @{@link GMTResponseDTO}
     */
    @GetMapping("/calculate")
    public ResponseEntity<GMTResponseDTO> calculateLoanPayment(
            @RequestParam Double loanAmount,
            @RequestParam Double interestRate,
            @RequestParam Integer loanTerm) {
        try {
            GMTResponseDTO response = gmtLoanService.calculateLoanPayment(loanAmount, interestRate, loanTerm);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Идэвхтэй зээлүүд авах
     *
     * @param req @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_LOAN_OFFICER')")
    @GetMapping("/active")
    public ResponseEntity<GMTResponseDTO> getActiveLoans(HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getActiveLoans][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.getActiveLoans(req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.getActiveLoans][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.getActiveLoans][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }

    /**
     * Зээлийн төлбөр төлөх
     *
     * @param loanId        зээлийн ID
     * @param paymentAmount төлбөрийн хэмжээ
     * @param req           @{@link HttpServletRequest}
     * @return @{@link GMTResponseDTO}
     */
    @PreAuthorize("hasRole('ROLE_USER') || hasRole('ROLE_CORPORATE')")
    @PostMapping("/{loanId}/payment")
    public ResponseEntity<GMTResponseDTO> processLoanPayment(
            @PathVariable String loanId,
            @RequestParam Double paymentAmount,
            HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.processLoanPayment][" + loanId + "][" + paymentAmount + "][init][" + req.getRemoteUser() + "]");
            
            GMTResponseDTO response = gmtLoanService.processLoanPayment(loanId, paymentAmount, req);
            
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[controller][loan.processLoanPayment][" + loanId + "][" + paymentAmount + "][end][" + req.getRemoteUser() + "]");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[controller][loan.processLoanPayment][" + loanId + "][" + paymentAmount + "][error][" + req.getRemoteUser() + "] " + e.getMessage());
            return ResponseEntity.badRequest().body(new GMTResponseDTO(400, e.getMessage(), null, null));
        }
    }
}
