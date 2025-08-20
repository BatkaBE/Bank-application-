package com.golomt.account.GMTController;

import com.golomt.account.GMTConstant.GMTLog;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTAccountRequestDTO;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTAccountUpdateDTO;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTAccountDTO.GMTBalanceUpdateDTO;
import com.golomt.account.GMTDTO.GMTRequestDTO.GMTCommonDTO.GMTRequestDTO;
import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTErrorDTO;
import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import com.golomt.account.GMTEntity.GMTAccountEntity;
import com.golomt.account.GMTException.GMTCustomException;
import com.golomt.account.GMTHelper.GMTHelper;
import com.golomt.account.GMTHelper.GMTResponse;
import com.golomt.account.GMTService.GMTAccountService;
import com.golomt.account.GMTUtility.GMTLOGUtilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
public class GMTAccountController {

    @Autowired
    private GMTAccountService accountService;

    @PostMapping("/create")
    public GMTResponseDTO createAccount(@RequestBody GMTRequestDTO<GMTAccountRequestDTO> request, HttpServletRequest req) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.createAccount][init]");

            if (request == null || request.getBody() == null) {
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(),"Хүсэлтийн бие шаардлагатай").getResponseDTO();
            }

            GMTAccountRequestDTO accountData = request.getBody();

            if (accountData.getAccountName() == null || accountData.getAccountName().trim().isEmpty()) {
                GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "Дансны нэр шаардлагатай");
                return new GMTResponse( HttpStatus.BAD_REQUEST.value(), "Дансны нэр шаардлагатай").getResponseDTO();
            }

            GMTResponseDTO response = accountService.createAccount(accountData, req);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.createAccount][амжилттай үүслээ]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.createAccount][end]");
            return new GMTResponse( HttpStatus.CREATED.value(), "Дансны амжилттай үүслээ").getResponseDTO();
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.createAccount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                    new GMTErrorDTO("404", "Дотоод серверийн алдаа")).getResponseDTO();
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.createAccount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }


    @GetMapping("/id/{id}")
    public GMTResponseDTO getAccountById(@PathVariable Long id) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountById][" + id + "][init]");

            GMTResponseDTO response = accountService.getAccountById(id);

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountById][" + id + "][end]");
            return response;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountById][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @GetMapping("/account-number/{accountNumber}")
    public GMTResponseDTO getAccountByAccountNumber(@PathVariable String accountNumber) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountByAccountNumber][" + accountNumber + "][init]");

            GMTResponseDTO response = accountService.getAccountByAccountNumber(accountNumber);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountByAccountNumber][амжилттай олдлоо]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountByAccountNumber][" + accountNumber + "][end]");
            return response;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountByAccountNumber][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @GetMapping("/all")
    public GMTResponseDTO getAllAccounts(@RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAllAccounts][page:" + page + ",size:" + size + "][init]");

            GMTResponseDTO response = accountService.getAllAccounts(page, size);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAllAccounts][");

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAllAccounts][амжилттай олдлоо]" + response.getBody());
            return response;
        } catch (Exception e) {
            log.error("Error getting all accounts", e);
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getAllAccounts][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @GetMapping("/username/{username}")
    public GMTResponseDTO getByUsername(@PathVariable String username) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][" + username + "][init]");

            GMTResponseDTO response = accountService.getAccountByUser(username);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][амжилттай олдлоо]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][" + username + "][end]");
            return response;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }


    @GetMapping("/type/{accountType}")
    public GMTResponseDTO getAccountsByType(@PathVariable String accountType) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][" + accountType + "][init]");

            GMTResponseDTO response = accountService.getAccountsByType(accountType);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][амжилттай олдлоо]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][" + accountType + "][end]");
            return response;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getAccountsByType][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @PutMapping("/{id}")
    public GMTResponseDTO updateAccount(@PathVariable Long id,
                                        @RequestBody GMTRequestDTO<GMTAccountUpdateDTO> request,
                                        HttpServletRequest httpRequest) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][" + id + "][init]");

            if (request == null || request.getBody() == null) {
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Хүсэлтийн бие шаардлагатай",
                        new GMTErrorDTO("400", "Хүсэлтийн body шаардлагатай")).getResponse();
            }

            GMTAccountUpdateDTO updateData = request.getBody();
            GMTAccountEntity account = new GMTAccountEntity();
            account.setId(id);
            if (updateData.getAccountName() != null) {
                account.setAccountName(updateData.getAccountName());
            }
            if (updateData.getAccountType() != null) {
                account.setAccountType(updateData.getAccountType());
            }
            if (updateData.getCurrencyCode() != null) {
                account.setCurrencyCode(updateData.getCurrencyCode());
            }
            account.setActive(updateData.isActive());
            account.setActive(updateData.isLocked());
            account.setUpdatedBy(GMTHelper.getCurrentUser(httpRequest));

            GMTResponseDTO response = accountService.updateAccount(account);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][амжилттай шинэчлэгдлээ]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][" + id + "][end]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                    new GMTErrorDTO("404", e.getMessage())).getResponse();
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @PutMapping("/{id}/balance")
    public GMTResponseDTO updateBalance(@PathVariable Long id,
                                        @RequestBody GMTRequestDTO<GMTBalanceUpdateDTO> request,
                                        HttpServletRequest httpRequest) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateBalance][" + id + "][init]");

            if (request == null || request.getBody() == null) {
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Хүсэлтийн бие шаардлагатай",
                        new GMTErrorDTO("001", "Хүсэлтийн бие шаардлагатай")).getResponse();
            }

            GMTBalanceUpdateDTO balanceData = request.getBody();
            if (balanceData.getAmountDelta() == null) {
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Дүн шаардлагатай",
                        new GMTErrorDTO("400", "Дүн шаардлагатай")).getResponse();
            }

            GMTResponseDTO response = accountService.updateBalance(id, balanceData.getAmountDelta());
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][амжилттай шинэчлэгдлээ]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateBalance][" + id + "][end]");
            return response;
        } catch (GMTCustomException e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.updateBalance][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                    new GMTErrorDTO("404", e.getMessage())).getResponse();
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.updateBalance][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @PutMapping("/account-number/{accountNumber}/adjust")
    public GMTResponseDTO adjustBalanceByAccountNumber(@PathVariable String accountNumber,
                                                       @RequestBody GMTRequestDTO<GMTBalanceUpdateDTO> request,
                                                       HttpServletRequest httpRequest) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.adjustBalance][" + accountNumber + "][init]");

            if (request == null || request.getBody() == null || request.getBody().getAmountDelta() == null) {
                return new GMTResponse(HttpStatus.BAD_REQUEST.value(), "Дүн шаардлагатай",
                        new GMTErrorDTO("400", "Дүн шаардлагатай")).getResponse();
            }

            GMTResponseDTO response = accountService.adjustBalanceByAccountNumber(accountNumber, request.getBody().getAmountDelta());
            return response;
        } catch (GMTCustomException e) {
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(), new GMTErrorDTO("404", e.getMessage())).getResponse();
        } catch (Exception e) {
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @DeleteMapping("/{id}")
    public GMTResponseDTO deleteAccount(@PathVariable Long id) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.deleteAccount][" + id + "][init]");

            GMTResponseDTO response = accountService.deleteAccount(id);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.updateAccount][амжилттай устлаа]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.deleteAccount][" + id + "][end]");
            return response;
        } catch (GMTCustomException e) {
            log.error("Validation error deleting account: {}", e.getMessage());
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.deleteAccount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                    new GMTErrorDTO("404", e.getMessage())).getResponse();
        } catch (Exception e) {
            log.error("Error deleting account with ID: {}", id, e);
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.deleteAccount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @GetMapping("/statistics/count")
    public GMTResponseDTO getTotalAccountCount() {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalAccountCount][init]");

            GMTResponseDTO response = accountService.getTotalAccountCount();
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalAccountCount][амжилттай олдлоо]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalAccountCount][end]");
            return response;
        } catch (Exception e) {
            log.error("Error getting account count", e);
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalAccountCount][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }

    @GetMapping("/statistics/balance/{currencyCode}")
    public GMTResponseDTO getTotalBalanceByCurrency(@PathVariable String currencyCode) {
        try {
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalBalanceByCurrency][" + currencyCode + "][init]");

            GMTResponseDTO response = accountService.getTotalBalanceByCurrency(currencyCode);
            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalBalanceByCurrency][амжилттай олдлоо]" + response.getBody());

            GMTLOGUtilities.info(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalBalanceByCurrency][" + currencyCode + "][end]");
            return response;
        } catch (Exception e) {
            GMTLOGUtilities.error(GMTLog.ACCOUNT.getValue(), "[controller][account.getTotalBalanceByCurrency][error] " + e.getMessage());
            return new GMTResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Дотоод серверийн алдаа",
                    new GMTErrorDTO("500", "Дотоод серверийн алдаа")).getResponse();
        }
    }


}