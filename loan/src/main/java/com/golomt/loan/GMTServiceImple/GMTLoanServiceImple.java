package com.golomt.loan.GMTServiceImple;

import com.golomt.loan.GMTConstant.GMTLog;
import com.golomt.loan.GMTConstant.GMTLoanStatus;
import com.golomt.loan.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.loan.GMTDTO.GMTRequestDTO.GMTLoanDTO.GMTLoanRequestDTO;
import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTLoanDTO.GMTLoanResponseDTO;
import com.golomt.loan.GMTEntity.GMTLoanEntity;
import com.golomt.loan.GMTException.*;
import com.golomt.loan.GMTHelper.GMTResponse;
import com.golomt.loan.GMTRepository.GMTLoanRepository;
import com.golomt.loan.GMTService.GMTLoanService;
import com.golomt.loan.GMTUtility.GMTLOGUtilities;
import com.golomt.loan.GMTUtility.GMTMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.golomt.loan.GMTUtility.GMTMapper.mapToResponse;

@Slf4j
@Transactional
@Service
public class GMTLoanServiceImple implements GMTLoanService {

    private final GMTLoanRepository gmtLoanRepository;

    @Inject
    public GMTLoanServiceImple(GMTLoanRepository gmtLoanRepository) {
        this.gmtLoanRepository = gmtLoanRepository;
    }

    public GMTLoanEntity createLoan(GMTLoanEntity loan) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.createLoan][" + loan.getLoanId().toLowerCase() + "][init]");

        if (gmtLoanRepository.getLoanByLoanId(loan.getLoanId()) != null) {
            throw new GMTCustomException(loan.getLoanId() + " тай зээл аль хэдийн үүссэн байна. ");
        }

        loan.setCreatedAt(LocalDateTime.now());
        loan.setUpdatedAt(LocalDateTime.now());

        if (loan.getStatus() == null) {
            loan.setStatus(String.valueOf(GMTLoanStatus.PENDING));
        }

        calculateLoanDetails(loan);

        gmtLoanRepository.save(loan);
        GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[Амжилттай хадгалсан..][loan: " + loan + "]");

        GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.createLoan][" + loan.getId().toString().toLowerCase() + "][end]");

        return loan;
    }

    public GMTResponseDTO applyForLoan(GMTRequestDTO<GMTLoanRequestDTO> dto, HttpServletRequest req) throws GMTValidationException, GMTCustomException, GMTRuntimeException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.applyForLoan][" + dto.getData().getLoanType().toLowerCase() + "][init][" + req.getRemoteUser() + "]");

            GMTLoanRequestDTO loanRequest = dto.getData();
            
            GMTLoanEntity loan = new GMTLoanEntity();
            loan.setLoanId(generateLoanId());
            loan.setUserId(loanRequest.getUserId());
            loan.setAccountNumber(loanRequest.getAccountNumber());
            loan.setLoanAmount(loanRequest.getLoanAmount());
            loan.setInterestRate(loanRequest.getInterestRate());
            loan.setLoanTerm(loanRequest.getLoanTerm());
            loan.setLoanType(loanRequest.getLoanType());
            loan.setPurpose(loanRequest.getPurpose());
            loan.setCurrencyCode(loanRequest.getCurrencyCode());
            loan.setCollateral(loanRequest.getCollateral());
            loan.setGuarantor(loanRequest.getGuarantor());
            loan.setNotes(loanRequest.getNotes());
            loan.setStatus(String.valueOf(GMTLoanStatus.PENDING));
            loan.setCreatedBy(req.getRemoteUser());
            loan.setStartDate(LocalDateTime.now());

            calculateLoanDetails(loan);

            GMTLoanEntity savedLoan = createLoan(loan);
            GMTLoanResponseDTO response = mapToResponse(savedLoan);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.applyForLoan][" + dto.getData().getLoanType().toLowerCase() + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[Амжилттай response ирлээ..][response: " + response + "]");
            
            return new GMTResponse(HttpStatus.OK.value(), "Зээлийн хүсэлт амжилттай илгээгдлээ", response).getResponseDTO();
            
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.applyForLoan][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.applyForLoan][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.applyForLoan][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO getLoanByLoanId(String loanId, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoanByLoanId][" + loanId + "][init][" + req.getRemoteUser() + "]");

            GMTLoanEntity loan = gmtLoanRepository.getLoanByLoanId(loanId);
            if (loan == null) {
                throw new GMTCustomException("Зээл олдсонгүй: " + loanId);
            }

            GMTLoanResponseDTO response = mapToResponse(loan);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoanByLoanId][" + loanId + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoanByLoanId][" + loanId + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoanByLoanId][" + loanId + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO getAllLoansByUser(String userId, HttpServletRequest req) throws GMTRuntimeException, GMTCustomException, GMTValidationException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getAllLoansByUser][" + userId + "][init][" + req.getRemoteUser() + "]");

            List<GMTLoanEntity> loans = gmtLoanRepository.getAllLoansByUserId(userId);
            List<GMTLoanResponseDTO> response = GMTMapper.mapToResponseList(loans);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getAllLoansByUser][" + userId + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getAllLoansByUser][" + userId + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTRuntimeException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getAllLoansByUser][" + userId + "][runtime][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getAllLoansByUser][" + userId + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO getLoansByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByAccountNumber][" + accountNumber + "][init][" + req.getRemoteUser() + "]");

            List<GMTLoanEntity> loans = gmtLoanRepository.findByAccountNumber(accountNumber);
            List<GMTLoanResponseDTO> response = GMTMapper.mapToResponseList(loans);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByAccountNumber][" + accountNumber + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoansByAccountNumber][" + accountNumber + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoansByAccountNumber][" + accountNumber + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByDateRange][" + startDate + " to " + endDate + "][init][" + req.getRemoteUser() + "]");

            List<GMTLoanEntity> loans = gmtLoanRepository.getLoansByStatusAndDateRange("ACTIVE", startDate, endDate);
            List<GMTLoanResponseDTO> response = GMTMapper.mapToResponseList(loans);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByDateRange][" + startDate + " to " + endDate + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoansByDateRange][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoansByDateRange][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO getLoansByAmountRange(Double minAmount, Double maxAmount, HttpServletRequest req) {
        GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByAmountRange][" + minAmount + " to " + maxAmount + "][init][" + req.getRemoteUser() + "]");

        List<GMTLoanEntity> loans = gmtLoanRepository.getLoansByAmountRange(minAmount, maxAmount);
        List<GMTLoanResponseDTO> response = GMTMapper.mapToResponseList(loans);

        GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByAmountRange][" + minAmount + " to " + maxAmount + "][end][" + req.getRemoteUser() + "]");
        GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
    }

    @Transactional
    public GMTResponseDTO updateLoanStatus(String loanId, String status, String updatedBy, String rejectionReason, HttpServletRequest req) {
        log.info("Updating loan status: {} to {}", loanId, status);
        GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.updateLoanStatus][" + loanId + " " + status + "][init][" + req.getRemoteUser() + "]");

        // Add null checks for required parameters
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new IllegalArgumentException("Зээлийн ID хоосон байж болохгүй");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Статус хоосон байж болохгүй");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Шинэчилсэн хүн хоосон байж болохгүй");
        }

        GMTLoanEntity loan = gmtLoanRepository.getLoanByLoanId(loanId);
        if (loan == null) {
            throw new GMTCustomException("Зээлийн ID олсонгүй: " + loanId);
        }

        loan.setStatus(status);
        loan.setUpdatedBy(updatedBy);
        loan.setUpdatedAt(LocalDateTime.now());

        if (status.equals(GMTLoanStatus.APPROVED.getValue())) {
            loan.setApprovedAt(LocalDateTime.now());
            loan.setApprovedBy(updatedBy);
        }

        if (status.equals(GMTLoanStatus.REJECTED.getValue()) && rejectionReason != null) {
            loan.setRejectionReason(rejectionReason);
        }

        GMTLoanEntity updatedLoan = gmtLoanRepository.save(loan);
        GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "response: " + updatedLoan);

        GMTLoanResponseDTO response = GMTMapper.mapToResponse(updatedLoan);
        GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.updateLoanStatus][" + loanId + " " + status + "][end][" + req.getRemoteUser() + "]");
        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай солигдлоо...", response).getResponseDTO();
    }

    public GMTResponseDTO approveLoan(String loanId, String approvedBy, HttpServletRequest req) {
        return updateLoanStatus(loanId, GMTLoanStatus.APPROVED.getValue(), approvedBy, null, req);
    }

    public GMTResponseDTO rejectLoan(String loanId, String rejectedBy, String rejectionReason, HttpServletRequest req) {
        return updateLoanStatus(loanId, GMTLoanStatus.REJECTED.getValue(), rejectedBy, rejectionReason, req);
    }

    public GMTResponseDTO getLoansByStatus(String status, String userId, HttpServletRequest req) throws GMTValidationException, GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByStatus][" + status + "][" + userId + "][init][" + req.getRemoteUser() + "]");

            List<GMTLoanEntity> loans = gmtLoanRepository.findByUserIdAndStatus(userId, status);
            List<GMTLoanResponseDTO> response = GMTMapper.mapToResponseList(loans);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getLoansByStatus][" + status + "][" + userId + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTValidationException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoansByStatus][" + status + "][" + userId + "][validation][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getLoansByStatus][" + status + "][" + userId + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    public GMTResponseDTO calculateLoanPayment(Double loanAmount, Double interestRate, Integer loanTerm) {
        // Add null checks for required parameters
        if (loanAmount == null || loanAmount <= 0) {
            throw new IllegalArgumentException("Зээлийн хэмжээ буруу байна");
        }
        if (interestRate == null || interestRate <= 0) {
            throw new IllegalArgumentException("Хүүгийн түвшин буруу байна");
        }
        if (loanTerm == null || loanTerm <= 0) {
            throw new IllegalArgumentException("Зээлийн хугацаа буруу байна");
        }

        double monthlyInterestRate = interestRate / 100 / 12;
        double monthlyPayment = loanAmount * (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanTerm)) / (Math.pow(1 + monthlyInterestRate, loanTerm) - 1);
        double totalAmount = monthlyPayment * loanTerm;
        double totalInterest = totalAmount - loanAmount;

        // Round to 2 decimal places for consistency
        monthlyPayment = Math.round(monthlyPayment * 100.0) / 100.0;
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        totalInterest = Math.round(totalInterest * 100.0) / 100.0;

        return new GMTResponse(HttpStatus.OK.value(), "Тооцоолсон",
            java.util.Map.of(
                "monthlyPayment", monthlyPayment,
                "totalAmount", totalAmount,
                "totalInterest", totalInterest
            )).getResponseDTO();
    }

    public GMTResponseDTO getActiveLoans(HttpServletRequest req) throws GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getActiveLoans][init][" + req.getRemoteUser() + "]");

            List<GMTLoanEntity> loans = gmtLoanRepository.getActiveLoans();
            List<GMTLoanResponseDTO> response = GMTMapper.mapToResponseList(loans);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.getActiveLoans][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай ирлээ...][response :" + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.getActiveLoans][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public GMTResponseDTO processLoanPayment(String loanId, Double paymentAmount, HttpServletRequest req) throws GMTCustomException {
        try {
            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.processLoanPayment][" + loanId + "][" + paymentAmount + "][init][" + req.getRemoteUser() + "]");

            // Add null checks for required parameters
            if (loanId == null || loanId.trim().isEmpty()) {
                throw new GMTCustomException("Зээлийн ID хоосон байж болохгүй");
            }
            if (paymentAmount == null || paymentAmount <= 0) {
                throw new GMTCustomException("Төлбөрийн хэмжээ буруу байна");
            }

            GMTLoanEntity loan = gmtLoanRepository.getLoanByLoanId(loanId);
            if (loan == null) {
                throw new GMTCustomException("Зээл олсонгүй: " + loanId);
            }

            if (loan.getStatus() == null || !loan.getStatus().equals(GMTLoanStatus.ACTIVE.getValue())) {
                throw new GMTCustomException("Зээл идэвхтэй биш байна");
            }

            if (loan.getRemainingBalance() == null) {
                throw new GMTCustomException("Зээлийн үлдэгдэл мэдээлэл алга байна");
            }

            if (paymentAmount > loan.getRemainingBalance()) {
                throw new GMTCustomException("Төлбөрийн хэмжээ үлдэгдэл хэмжээнээс их байна");
            }

            loan.setRemainingBalance(loan.getRemainingBalance() - paymentAmount);
            loan.setUpdatedBy(req.getRemoteUser());
            loan.setUpdatedAt(LocalDateTime.now());

            if (loan.getRemainingBalance() <= 0) {
                loan.setStatus(GMTLoanStatus.COMPLETED.getValue());
                loan.setEndDate(LocalDateTime.now());
            }

            GMTLoanEntity updatedLoan = gmtLoanRepository.save(loan);
            GMTLoanResponseDTO response = GMTMapper.mapToResponse(updatedLoan);

            GMTLOGUtilities.info(GMTLog.LOAN.getValue(), "[service][loan.processLoanPayment][" + loanId + "][" + paymentAmount + "][end][" + req.getRemoteUser() + "]");
            GMTLOGUtilities.debug(GMTLog.LOAN.getValue(), "[амжилттай response ирлээ..][response: " + response + "]");

            return new GMTResponse(HttpStatus.OK.value(), "Төлбөр амжилттай төлөгдлөө", response).getResponseDTO();
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.LOAN.getValue(), "[service][loan.processLoanPayment][" + loanId + "][" + paymentAmount + "][custom][" + req.getRemoteUser() + "] " + e.getMessage());
            throw e;
        }
    }

    private void calculateLoanDetails(GMTLoanEntity loan) {
        double monthlyInterestRate = loan.getInterestRate() / 100 / 12;
        double monthlyPayment = loan.getLoanAmount() * (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loan.getLoanTerm())) / (Math.pow(1 + monthlyInterestRate, loan.getLoanTerm()) - 1);

        double totalAmount = monthlyPayment * loan.getLoanTerm();
        double totalInterest = totalAmount - loan.getLoanAmount();

        // Round to 2 decimal places for consistency
        monthlyPayment = Math.round(monthlyPayment * 100.0) / 100.0;
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        totalInterest = Math.round(totalInterest * 100.0) / 100.0;

        loan.setMonthlyPayment(monthlyPayment);
        loan.setTotalAmount(totalAmount);
        loan.setRemainingBalance(totalAmount);

        // Set total interest if the entity has this field (add it if missing)
        // loan.setTotalInterest(totalInterest);
    }

    private String generateLoanId() {
        return "LOAN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
