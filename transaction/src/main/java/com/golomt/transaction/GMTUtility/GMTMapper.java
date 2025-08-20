package com.golomt.transaction.GMTUtility;

import com.golomt.transaction.GMTDTO.GMTResponseDTO.GMTTransactionDTO.GMTTransactionResponseDTO;
import com.golomt.transaction.GMTEntity.GMTTransactionEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GMTMapper {
    public static GMTTransactionResponseDTO mapToResponse(GMTTransactionEntity transaction) {
        GMTTransactionResponseDTO response = new GMTTransactionResponseDTO();
        response.setTransactionId(transaction.getTransactionId());
        response.setFromAccountNumber(transaction.getFromAccountNumber());
        response.setToAccountNumber(transaction.getToAccountNumber());
        response.setAmount(transaction.getAmount());
        response.setCurrencyCode(transaction.getCurrencyCode());
        response.setStatus(transaction.getStatus());
        response.setTransactionType(transaction.getTransactionType());
        response.setDescription(transaction.getDescription());
        response.setReference(transaction.getReference());
        response.setFee(transaction.getFee());
        response.setProcessedAt(LocalDateTime.now());
        response.setFailureReason(transaction.getFailureReason());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        response.setCreatedBy(transaction.getCreatedBy());
        response.setUpdatedBy(transaction.getUpdatedBy());
        response.setId(transaction.getId());
        response.setToUserId(transaction.getToUserId());
        return response;
    }

    public static List<GMTTransactionResponseDTO> mapToResponseList(List<GMTTransactionEntity> transactions) {
        List<GMTTransactionResponseDTO> responses = new ArrayList<GMTTransactionResponseDTO>();
        for (GMTTransactionEntity transaction : transactions) {
            GMTTransactionResponseDTO response = new GMTTransactionResponseDTO();
            response.setTransactionId(transaction.getTransactionId());
            response.setFromAccountNumber(transaction.getFromAccountNumber());
            response.setToAccountNumber(transaction.getToAccountNumber());
            response.setAmount(transaction.getAmount());
            response.setCurrencyCode(transaction.getCurrencyCode());
            response.setStatus(transaction.getStatus());
            response.setTransactionType(transaction.getTransactionType());
            response.setDescription(transaction.getDescription());
            response.setReference(transaction.getReference());
            response.setFee(transaction.getFee());
            response.setProcessedAt(LocalDateTime.now());
            response.setFailureReason(transaction.getFailureReason());
            response.setCreatedAt(transaction.getCreatedAt());
            response.setUpdatedAt(transaction.getUpdatedAt());
            response.setCreatedBy(transaction.getCreatedBy());
            response.setUpdatedBy(transaction.getUpdatedBy());
            response.setId(transaction.getId());
            response.setToUserId(transaction.getToUserId());
            responses.add(response);
        }
        return responses;
    }
}
