package com.golomt.loan.GMTUtility;

import com.golomt.loan.GMTDTO.GMTResponseDTO.GMTLoanDTO.GMTLoanResponseDTO;
import com.golomt.loan.GMTEntity.GMTLoanEntity;

import java.util.List;
import java.util.stream.Collectors;

public class GMTMapper {

    public static GMTLoanResponseDTO mapToResponse(GMTLoanEntity loan) {
        if (loan == null) {
            return null;
        }

        GMTLoanResponseDTO response = new GMTLoanResponseDTO();
        response.setId(loan.getId());
        response.setLoanId(loan.getLoanId());
        response.setUserId(loan.getUserId());
        response.setAccountNumber(loan.getAccountNumber());
        response.setLoanAmount(loan.getLoanAmount());
        response.setInterestRate(loan.getInterestRate());
        response.setLoanTerm(loan.getLoanTerm());
        response.setLoanType(loan.getLoanType());
        response.setStatus(loan.getStatus());
        response.setMonthlyPayment(loan.getMonthlyPayment());
        response.setTotalAmount(loan.getTotalAmount());
        response.setRemainingBalance(loan.getRemainingBalance());
        response.setCurrencyCode(loan.getCurrencyCode());
        response.setPurpose(loan.getPurpose());
        response.setCollateral(loan.getCollateral());
        response.setGuarantor(loan.getGuarantor());
        response.setStartDate(loan.getStartDate());
        response.setEndDate(loan.getEndDate());
        response.setCreatedAt(loan.getCreatedAt());
        response.setUpdatedAt(loan.getUpdatedAt());
        response.setCreatedBy(loan.getCreatedBy());
        response.setUpdatedBy(loan.getUpdatedBy());
        response.setApprovedAt(loan.getApprovedAt());
        response.setApprovedBy(loan.getApprovedBy());
        response.setRejectionReason(loan.getRejectionReason());
        response.setNotes(loan.getNotes());

        return response;
    }

    public static List<GMTLoanResponseDTO> mapToResponseList(List<GMTLoanEntity> loans) {
        if (loans == null) {
            return null;
        }

        return loans.stream()
                .map(GMTMapper::mapToResponse)
                .collect(Collectors.toList());
    }
}
