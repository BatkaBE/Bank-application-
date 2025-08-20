package com.golomt.account.GMTService;

import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTAccountCreateRequestDTO;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTAccountRequestDTO;
import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.account.GMTEntity.GMTAccountEntity;
import com.golomt.account.GMTException.GMTCustomException;
import com.golomt.account.GMTException.GMTValidationException;

import javax.servlet.http.HttpServletRequest;

public interface GMTAccountService {
    GMTResponseDTO createAccount(GMTAccountRequestDTO dto, HttpServletRequest req) throws GMTCustomException;

    GMTResponseDTO getAccountById(Long id);

    GMTResponseDTO getAccountByAccountNumber(String accountNumber);

    GMTResponseDTO getAllAccounts(int page, int size);

    GMTResponseDTO getAccountsByType(String accountType);

    GMTResponseDTO getAccountsByCurrency(String currencyCode);

    GMTResponseDTO updateAccount(GMTAccountEntity account) throws GMTCustomException;

    GMTResponseDTO updateBalance(Long accountId, Double newBalance) throws GMTCustomException;

    GMTResponseDTO adjustBalanceByAccountNumber(String accountNumber, Double amountDelta) throws GMTCustomException;

    GMTResponseDTO deleteAccount(Long id) throws GMTCustomException;

    GMTResponseDTO existsByAccountNumber(String accountNumber);

    GMTResponseDTO getAccountsByCreatedBy(String createdBy);

    GMTResponseDTO getTotalAccountCount();

    GMTResponseDTO getTotalBalanceByCurrency(String currencyCode);

    GMTResponseDTO getAccountByUser(String username);

}