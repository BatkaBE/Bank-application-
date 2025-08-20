package com.golomt.account.GMTHelper;

import com.golomt.account.GMTException.GMTCustomException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;



public class GMTHelper {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static boolean isValidAccountFormat(String account) {
        return account != null && account.matches("[0-9]{10}");
    }

    public static boolean isSupportedCurrency(String currency) {
        return currency != null && ("MNT".equals(currency) || "USD".equals(currency) || "EUR".equals(currency));
    }
    public static String generateAccountNumber() {
        long min = 1_000_000_000L;
        long max = 9_999_999_999L;
        long randomNum = min + (long)(Math.random() * (max - min + 1));
        String newAccountNumber = String.valueOf(randomNum);
        
        // Validate the generated number
        if (isValidAccountFormat(newAccountNumber)) {
            return newAccountNumber;
        } else {
            // If invalid, generate a new one (but this should rarely happen)
            // Use a different approach to avoid potential infinite recursion
            randomNum = min + (long)(Math.random() * (max - min + 1));
            newAccountNumber = String.valueOf(randomNum);
            return isValidAccountFormat(newAccountNumber) ? newAccountNumber : "1000000000"; // Fallback
        }
    }

    public static String getCurrentUser(HttpServletRequest request) {
        String user = request.getHeader("X-User-ID");
        return user != null ? user : "system";
    }
    public static Long resolveAccountIdByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTCustomException {
        try {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                throw new GMTCustomException("Дансны дугаар хоосон байна");
            }
            
            String base = "http://localhost:8085/api/accounts";
            HttpHeaders headers = new HttpHeaders();
            String auth = req.getHeader("Authorization");
            if (auth != null) headers.set("Authorization", auth);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            String url = base + "/account-number/" + accountNumber.trim();
            System.out.println("DEBUG: Calling URL: " + url);
            
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new GMTCustomException("Дансны мэдээлэл авахад алдаа гарлаа. Статус: " + resp.getStatusCode());
            }
            
            Map<?, ?> wrapper = resp.getBody();
            if (wrapper == null) {
                System.out.println("DEBUG: Response body is null");
                throw new GMTCustomException("Хариу хоосон байна");
            }
            
            System.out.println("DEBUG: Response wrapper: " + wrapper);
            
            Object body = wrapper.get("body");
            if (body == null) {
                System.out.println("DEBUG: Body field is null in wrapper");
                throw new GMTCustomException("Body талбар хоосон байна");
            }
            
            if (!(body instanceof Map)) {
                System.out.println("DEBUG: Body is not a Map, it's: " + body.getClass().getSimpleName() + " = " + body);
                throw new GMTCustomException("Body буруу байна - Map биш: " + body.getClass().getSimpleName());
            }
            
            Map<?, ?> bodyMap = (Map<?, ?>) body;
            System.out.println("DEBUG: Body map: " + bodyMap);
            
            Object idVal = bodyMap.get("id");
            if (idVal == null) {
                System.out.println("DEBUG: ID field is null in body");
                throw new GMTCustomException("ID талбар хоосон байна");
            }
            
            System.out.println("DEBUG: ID value: " + idVal + " (type: " + idVal.getClass().getSimpleName() + ")");
            
            if (idVal instanceof Number) {
                Long result = ((Number) idVal).longValue();
                System.out.println("DEBUG: Returning ID: " + result);
                return result;
            }
            
            throw new GMTCustomException("ID олдсонгүй эсвэл тоо биш: " + idVal);
            
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in resolveAccountIdByAccountNumber: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof GMTCustomException) {
                throw e;
            }
            throw new GMTCustomException("Дансны мэдээлэл авахад алдаа гарлаа: " + e.getMessage());
        }
    }

    public static void adjustAccountBalanceById(Long accountId, double amountDelta, HttpServletRequest req) throws GMTCustomException {
        try {
            if (accountId == null) {
                throw new GMTCustomException("Дансны ID хоосон байна");
            }
            
            // First get the account number by ID
            String accountNumber = getAccountNumberById(accountId, req);
            
            // Then adjust balance using account number
            adjustAccountBalanceByAccountNumber(accountNumber, amountDelta, req);
            
        } catch (Exception e) {
            if (e instanceof GMTCustomException) {
                throw e;
            }
            throw new GMTCustomException("Үлдэгдэл шинэчлэхэд алдаа гарлаа: " + e.getMessage());
        }
    }

    public static void adjustAccountBalanceByAccountNumber(String accountNumber, double amountDelta, HttpServletRequest req) throws GMTCustomException {
        try {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                throw new GMTCustomException("Дансны дугаар хоосон байна");
            }
            
            String base = "http://localhost:8085/api/accounts";
            Map<String, Object> body = new HashMap<>();
            body.put("amountDelta", amountDelta);
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("body", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = req.getHeader("Authorization");
            if (auth != null) headers.set("Authorization", auth);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(wrapper, headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                base + "/account-number/" + accountNumber.trim() + "/adjust", 
                HttpMethod.PUT, 
                entity, 
                Void.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new GMTCustomException("Үлдэгдэл шинэчлэхэд алдаа гарлаа. Статус: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            if (e instanceof GMTCustomException) {
                throw e;
            }
            throw new GMTCustomException("Үлдэгдэл шинэчлэхэд алдаа гарлаа: " + e.getMessage());
        }
    }

    private static String getAccountNumberById(Long accountId, HttpServletRequest req) throws GMTCustomException {
        try {
            String base = "http://localhost:8085/api/accounts";
            HttpHeaders headers = new HttpHeaders();
            String auth = req.getHeader("Authorization");
            if (auth != null) headers.set("Authorization", auth);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> resp = restTemplate.exchange(
                    base + "/id/" + accountId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new GMTCustomException("Дансны мэдээлэл авахад алдаа гарлаа. Статус: " + resp.getStatusCode());
            }
            
            Map<?, ?> wrapper = resp.getBody();
            if (wrapper == null) {
                throw new GMTCustomException("Хариу хоосон байна");
            }
            
            Object body = wrapper.get("body");
            if (!(body instanceof Map)) {
                throw new GMTCustomException("Хариуны формат буруу байна");
            }
            
            Object accountNumberVal = ((Map<?, ?>) body).get("accountNumber");
            if (accountNumberVal instanceof String) {
                return (String) accountNumberVal;
            }
            
            throw new GMTCustomException("Дансны дугаар олдсонгүй: " + accountId);
            
        } catch (Exception e) {
            if (e instanceof GMTCustomException) {
                throw e;
            }
            throw new GMTCustomException("Дансны мэдээлэл авахад алдаа гарлаа: " + e.getMessage());
        }
    }

    public static double getAccountBalanceByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTCustomException {
        try {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                throw new GMTCustomException("Дансны дугаар хоосон байна");
            }
            
            String base = "http://localhost:8085/api/accounts";
            HttpHeaders headers = new HttpHeaders();
            String auth = req.getHeader("Authorization");
            if (auth != null) headers.set("Authorization", auth);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            String url = base + "/account-number/" + accountNumber.trim();
            System.out.println("DEBUG: Calling URL: " + url);
            
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new GMTCustomException("Дансны үлдэгдэл авахад алдаа гарлаа. Статус: " + resp.getStatusCode());
            }
            
            Map<?, ?> wrapper = resp.getBody();
            if (wrapper == null) {
                System.out.println("DEBUG: Response body is null");
                throw new GMTCustomException("Хариу хоосон байна");
            }
            
            System.out.println("DEBUG: Response wrapper: " + wrapper);
            
            Object body = wrapper.get("body");
            if (body == null) {
                System.out.println("DEBUG: Body field is null in wrapper");
                throw new GMTCustomException("Body талбар хоосон байна");
            }
            
            if (!(body instanceof Map)) {
                System.out.println("DEBUG: Body is not a Map, it's: " + body.getClass().getSimpleName() + " = " + body);
                throw new GMTCustomException("Body буруу байна - Map биш: " + body.getClass().getSimpleName());
            }
            
            Map<?, ?> bodyMap = (Map<?, ?>) body;
            System.out.println("DEBUG: Body map: " + bodyMap);
            
            Object balanceVal = bodyMap.get("balance");
            if (balanceVal == null) {
                System.out.println("DEBUG: Balance field is null in body");
                throw new GMTCustomException("Үлдэгдэл олдсонгүй");
            }
            
            System.out.println("DEBUG: Balance value: " + balanceVal + " (type: " + balanceVal.getClass().getSimpleName() + ")");
            
            if (balanceVal instanceof Number) {
                double result = ((Number) balanceVal).doubleValue();
                System.out.println("DEBUG: Returning balance: " + result);
                return result;
            }
            if (balanceVal instanceof String) {
                try {
                    double result = Double.parseDouble((String) balanceVal);
                    System.out.println("DEBUG: Parsed balance from string: " + result);
                    return result;
                } catch (NumberFormatException e) {
                    throw new GMTCustomException("Үлдэгдэл тоо биш: " + balanceVal);
                }
            }
            
            throw new GMTCustomException("Үлдэгдэл олдсонгүй эсвэл тоо биш: " + balanceVal);
            
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getAccountBalanceByAccountNumber: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof GMTCustomException) {
                throw e;
            }
            throw new GMTCustomException("Дансны үлдэгдэл авахад алдаа гарлаа: " + e.getMessage());
        }
    }


}
