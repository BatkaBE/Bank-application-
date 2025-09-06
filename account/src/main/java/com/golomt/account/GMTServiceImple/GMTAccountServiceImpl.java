package com.golomt.account.GMTServiceImple;

import com.golomt.account.GMTConstant.GMTLog;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTAccountCreateRequestDTO;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTAccountRequestDTO;
import com.golomt.account.GMTDTO.GMTResponseDTO.GMTAccountDTO.GMTAccountResponseDTO;
import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.account.GMTEntity.GMTAccountEntity;
import com.golomt.account.GMTException.GMTCustomException;
import com.golomt.account.GMTException.GMTValidationException;
import com.golomt.account.GMTHelper.GMTHelper;
import com.golomt.account.GMTHelper.GMTResponse;
import com.golomt.account.GMTRepository.GMTAccountRepository;
import com.golomt.account.GMTService.GMTAccountService;
import com.golomt.account.GMTUtility.GMTLOGUtilities;
import com.golomt.account.GMTUtility.GMTMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class GMTAccountServiceImpl implements GMTAccountService {

    @Inject
    private GMTAccountRepository accountRepository;

    @Override
    public GMTResponseDTO createAccount(GMTAccountRequestDTO dto, HttpServletRequest req) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.createAccount][" + dto.getAccountType() + "][init]");

        if (dto.getAccountName() == null || dto.getAccountName().trim().isEmpty()) {
            throw new GMTCustomException("Дансны нэр заавал оруулна уу");
        }
        if (dto.getAccountType() == null || dto.getAccountType().trim().isEmpty()) {
            throw new GMTCustomException("Дансны төрөл заавал оруулна уу");
        }
        if (dto.getCurrencyCode() == null || dto.getCurrencyCode().trim().isEmpty()) {
            throw new GMTCustomException("Валютын код заавал оруулна уу");
        }

        String accountNumber;
        int maxRetries = 10;
        int retryCount = 0;
        
        do {
            accountNumber = GMTHelper.generateAccountNumber();
            retryCount++;
            if (retryCount > maxRetries) {
                throw new GMTCustomException("Дансны дугаар үүсгэхэд алдаа гарлаа. Дахин оролдоно уу.");
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));

        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.createAccount][Generated account number: " + accountNumber + "]");

        GMTAccountEntity savedAccount = new GMTAccountEntity();
        savedAccount.setAccountNumber(accountNumber);
        savedAccount.setAccountName(dto.getAccountName().trim());
        savedAccount.setAccountType(dto.getAccountType().trim());
        savedAccount.setCurrencyCode(dto.getCurrencyCode().trim());
        savedAccount.setBalance(0.0); 
        savedAccount.setActive(true);
        savedAccount.setLocked(false);
        savedAccount.setCreatedAt(LocalDateTime.now());
        savedAccount.setUpdatedAt(LocalDateTime.now());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = "system";
        if (authentication != null && authentication.getDetails() instanceof Claims) {
            Claims claims = (Claims) authentication.getDetails();
            createdBy = claims.getSubject();
        }
        savedAccount.setCreatedBy(createdBy);

        if (dto.getBalance() != null && dto.getBalance() > 0) {
            if (dto.isSystemcreated()) {
                savedAccount.setBalance(dto.getBalance());
                savedAccount = accountRepository.save(savedAccount);
            } else if (dto.getFromAccountNumber() != null && !dto.getFromAccountNumber().trim().isEmpty()) {
                try {
                    double fromBalance = GMTHelper.getAccountBalanceByAccountNumber(dto.getFromAccountNumber(), req);
                    if (fromBalance < dto.getBalance()) {
                        throw new GMTCustomException("Эх дансны үлдэгдэл хүрэлцэхгүй. Хүссэн дүн: " + dto.getBalance() + ", Боломжит дүн: " + fromBalance);
                    }

                    savedAccount = accountRepository.save(savedAccount);

                    GMTHelper.adjustAccountBalanceByAccountNumber(dto.getFromAccountNumber(), -dto.getBalance(), req);
                    GMTHelper.adjustAccountBalanceByAccountNumber(savedAccount.getAccountNumber(), dto.getBalance(), req);

                    savedAccount.setBalance(dto.getBalance());
                    savedAccount = accountRepository.save(savedAccount);

                } catch (Exception e) {
                    if (savedAccount.getId() != null) {
                        accountRepository.deleteById(savedAccount.getId());
                    }
                    throw new GMTCustomException("Эх данснаас мөнгө шилжүүлэхэд алдаа гарлаа: " + e.getMessage());
                }
            } else {
                savedAccount.setBalance(dto.getBalance());
                savedAccount = accountRepository.save(savedAccount);
            }
        } else {
            savedAccount = accountRepository.save(savedAccount);
        }

        GMTAccountResponseDTO response = GMTMapper.mapToResponse(savedAccount);
        log.info("Данс амжилттай нээгдлээ: {} - {}", savedAccount.getAccountNumber(), savedAccount.getAccountName());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.createAccount][Данс амжилттай үүслээ]" + response);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.createAccount][" + savedAccount.getAccountNumber() + "][end]");

        return new GMTResponse(HttpStatus.CREATED.value(), "Данс амжилттай нээгдлээ", response).getResponseDTO();
    }


    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAccountById(Long id) {
        log.debug("Id -гаар данс татах: {}", id);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountById][" + id + "][init]");

        Optional<GMTAccountEntity> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountById][" + id + "][Данс олдсонгүй...]");
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), "Данс олдсонгүй").getResponseDTO();
        }

        GMTAccountResponseDTO response = GMTMapper.mapToResponse(accountOpt.get());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountById][Данс амжилттай олдлоо]" + response);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountById][" + id + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAccountByAccountNumber(String accountNumber) {
        log.debug("Fetching account by account number: {}", accountNumber);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByAccountNumber][" + accountNumber + "][init]");

        Optional<GMTAccountEntity> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByAccountNumber][" + accountNumber + "][Данс олдсонгүй...]");
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), "Данс олдсонгүй").getResponseDTO();
        }

        GMTAccountResponseDTO response = GMTMapper.mapToResponse(accountOpt.get());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByAccountNumber][Данс амжилттай олдлоо]" + response);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByAccountNumber][" + accountNumber + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", response).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAllAccounts(int page, int size) {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAllAccounts][" + page + "," + size + "][init]");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<GMTAccountEntity> accounts = accountRepository.findAll(pageable).getContent();

        List<GMTAccountResponseDTO> responseList = accounts.stream().map(GMTMapper::mapToResponse).collect(Collectors.toList());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAllAccounts][Данснууд амжилттай олдлоо]" + responseList);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAllAccounts][" + page + "," + size + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", responseList).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAccountByUser(String username) {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByUser][" + username + "][init]");

        List<GMTAccountEntity> accounts = accountRepository.findByCreatedBy(username);

        List<GMTAccountResponseDTO> responseList = accounts.stream().map(GMTMapper::mapToResponse).collect(Collectors.toList());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByUser][Данснууд амжилттай олдлоо]" + responseList);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountByUser][" + username + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", responseList).getResponseDTO();
    }


    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAccountsByType(String accountType) {
        log.debug("Fetching accounts by type: {}", accountType);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByType][" + accountType + "][init]");

        List<GMTAccountEntity> accounts = accountRepository.findByAccountType(accountType);
        List<GMTAccountResponseDTO> responseList = accounts.stream().map(GMTMapper::mapToResponse).collect(Collectors.toList());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByType][Данснууд амжилттай олдлоо]" + responseList);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByType][" + accountType + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", responseList).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAccountsByCurrency(String currencyCode) {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByCurrency][" + currencyCode + "][init]");

        List<GMTAccountEntity> accounts = accountRepository.findByCurrencyCode(currencyCode);
        List<GMTAccountResponseDTO> responseList = accounts.stream().map(GMTMapper::mapToResponse).collect(Collectors.toList());
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByCurrency][Данснууд амжилттай олдлоо]" + responseList);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByCurrency][" + currencyCode + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", responseList).getResponseDTO();
    }

    @Override
    public GMTResponseDTO updateAccount(GMTAccountEntity account) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.updateAccount][" + account.getId() + "][init]");

        Optional<GMTAccountEntity> existingAccountOpt = accountRepository.findById(account.getId());
        if (existingAccountOpt.isEmpty()) {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.updateAccount][" + account.getId() + "][Данс олдсонгүй...]");
            throw new GMTCustomException("Данс олдсонгүй: " + account.getId());
        }

        GMTAccountEntity existingAccount = existingAccountOpt.get();
        if (account.getAccountName() != null) {
            existingAccount.setAccountName(account.getAccountName());
        }
        if (account.getAccountType() != null) {
            existingAccount.setAccountType(account.getAccountType());
        }
        if (account.getCurrencyCode() != null) {
            existingAccount.setCurrencyCode(account.getCurrencyCode());
        }
        existingAccount.setActive(account.isLocked());

        existingAccount.setActive(account.isActive());
        existingAccount.setUpdatedAt(LocalDateTime.now());

        GMTAccountEntity updatedAccount = accountRepository.save(existingAccount);

        GMTAccountResponseDTO response = GMTMapper.mapToResponse(updatedAccount);
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.updateAccount][Данс амжилттай шинэчлэгдлээ]" + response);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.updateAccount][" + account.getId() + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай шинэчлэгдлээ", response).getResponseDTO();
    }

    @Override
    public GMTResponseDTO updateBalance(Long accountId, Double amountDelta) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.updateBalance][" + accountId + "][init]");

        Optional<GMTAccountEntity> accountOpt = accountRepository.findById(accountId);

        if (accountOpt.isEmpty()) {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.updateBalance][" + accountId + "][Данс олдсонгүй...]");
            throw new GMTCustomException("Данс олдсонгүй: " + accountId);
        }

        GMTAccountEntity account = accountOpt.get();
        account.setBalance(account.getBalance() + amountDelta);
        account.setUpdatedAt(LocalDateTime.now());

        GMTAccountEntity updatedAccount = accountRepository.save(account);

        GMTAccountResponseDTO response = GMTMapper.mapToResponse(updatedAccount);
        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.updateBalance][Үлдэгдэл амжилттай шинэчлэгдлээ]" + response);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.updateBalance][" + accountId + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай шинэчлэгдлээ", response).getResponseDTO();
    }

    @Override
    public GMTResponseDTO adjustBalanceByAccountNumber(String accountNumber, Double amountDelta) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.adjustBalanceByAccountNumber][" + accountNumber + "][init]");

        Optional<GMTAccountEntity> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            throw new GMTCustomException("Данс олдсонгүй: " + accountNumber);
        }

        GMTAccountEntity account = accountOpt.get();
        double newBalance = account.getBalance() + amountDelta;
        if (newBalance < 0) {
            throw new GMTCustomException("Үлдэгдэл хүрэлцэхгүй: " + accountNumber);
        }
        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());

        GMTAccountEntity updated = accountRepository.save(account);

        GMTAccountResponseDTO response = GMTMapper.mapToResponse(updated);
        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай шинэчлэгдлээ", response).getResponseDTO();
    }

    @Override
    public GMTResponseDTO deleteAccount(Long id) throws GMTCustomException {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.deleteAccount][" + id + "][init]");

        if (!accountRepository.existsById(id)) {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.deleteAccount][" + id + "][Данс олдсонгүй...]");
            throw new GMTCustomException("Данс олдсонгүй: " + id);
        }

        accountRepository.deleteById(id);

        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.deleteAccount][Данс амжилттай устлаа]");
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.deleteAccount][" + id + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай устлаа").getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO existsByAccountNumber(String accountNumber) {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.existsByAccountNumber][" + accountNumber + "][init]");

        boolean exists = accountRepository.existsByAccountNumber(accountNumber);

        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.existsByAccountNumber][Данс байгаа эсэх: " + exists + "]");
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.existsByAccountNumber][" + accountNumber + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай шалгалаа", exists).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getAccountsByCreatedBy(String createdBy) {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByCreatedBy][" + createdBy + "][init]");

        List<GMTAccountEntity> accounts = accountRepository.findByCreatedBy(createdBy);
        List<GMTAccountResponseDTO> responseList = accounts.stream().map(GMTMapper::mapToResponse).collect(Collectors.toList());

        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByCreatedBy][Данснууд амжилттай олдлоо]" + responseList);
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getAccountsByCreatedBy][" + createdBy + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", responseList).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTotalAccountCount() {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getTotalAccountCount][init]");

        long count = accountRepository.count();

        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getTotalAccountCount][Нийт дансны тоо: " + count + "]");
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getTotalAccountCount][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", count).getResponseDTO();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public GMTResponseDTO getTotalBalanceByCurrency(String currencyCode) {
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getTotalBalanceByCurrency][" + currencyCode + "][init]");

        Double total = accountRepository.sumBalanceByCurrencyCode(currencyCode);

        GMTLOGUtilities.debug(GMTLog.ACCOUNT.getValue(), "[service][account.getTotalBalanceByCurrency][Нийт үлдэгдэл: " + total + "]");
        GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[service][account.getTotalBalanceByCurrency][" + currencyCode + "][end]");

        return new GMTResponse(HttpStatus.OK.value(), "Амжилттай олдлоо", total).getResponseDTO();
    }


}