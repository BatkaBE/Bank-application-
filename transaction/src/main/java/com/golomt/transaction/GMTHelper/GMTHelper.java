package com.golomt.transaction.GMTHelper;

import com.golomt.transaction.GMTConstant.GMTCurrencyConstant;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTDepositRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTTransactionRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTWithdrawalRequestDTO;
import com.golomt.transaction.GMTDTO.GMTRequestDTO.GMTTransactionDTO.GMTInterBankTransactionRequestDTO;
import com.golomt.transaction.GMTEntity.GMTTransactionEntity;
import com.golomt.transaction.GMTException.GMTCustomException;
import com.golomt.transaction.GMTException.GMTValidationException;

import net.minidev.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.json.JSONArray;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.common.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.ByteArrayOutputStream;


public class GMTHelper {
    private static final RestTemplate restTemplate = new RestTemplate();
    public static boolean isValidAccountFormat(String account) {
        if (account == "EXTERNAL") return true;
        else {
            return account != null && account.matches("[0-9]{10,20}");
        }
    }

    public static boolean isInterBankAccount(String accountNumber) {

        if (accountNumber == null) return false;
        
        String cleanAccount = accountNumber.trim().replaceAll("[\\s\\-_]", "");

        if (cleanAccount.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$")) {
            return true;
        }
        
        // SWIFT/BIC format
        if (cleanAccount.matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
            return true;
        }
        
        // Specific country prefixes
        String[] countryPrefixes = {
            "GB", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "CH", "SE", "NO", "DK", "FI", 
            "PL", "CZ", "HU", "RO", "BG", "HR", "SI", "SK", "LT", "LV", "EE", "CY", "MT", 
            "LU", "IE", "PT", "GR", "US", "CA", "AU", "JP", "CN", "IN", "BR", "MX", "AR", 
            "CL", "CO", "PE", "VE", "EC", "BO", "PY", "UY", "GY", "SR", "FK", "GF"
        };
        
        for (String prefix : countryPrefixes) {
            if (cleanAccount.startsWith(prefix)) {
                return true;
            }
        }
        
        // Special prefixes
        String[] specialPrefixes = {"EXT", "SWIFT", "IBAN", "INTER", "FOREIGN"};
        for (String prefix : specialPrefixes) {
            if (cleanAccount.toUpperCase().startsWith(prefix)) {
                return true;
            }
        }
        
        // Contains letters (likely international)
        if (cleanAccount.matches(".*[A-Z].*")) {
            return true;
        }
        
        // Very long account numbers (likely international)
        if (cleanAccount.length() > 20) {
            return true;
        }
        
        // Default: assume internal account
        return false;
    }

    private static boolean isSupportedCurrency(String currency) {
        return currency != null && (GMTCurrencyConstant.MNT.value().equals(currency) || GMTCurrencyConstant.USD.value().equals(currency) || GMTCurrencyConstant.EUR.value().equals(currency));
    }

    public static void validateTransactionReq(GMTTransactionRequestDTO dto) throws GMTValidationException {
        try {
            if (dto == null) {
                throw new GMTValidationException("Гүйлгээний өгөгдөл дутуу байна");
            }
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                throw new GMTValidationException("Гүйлгээний мөнгөн дүн дутуу байна");
            }
            if (dto.getFromAccountNumber() == null || dto.getFromAccountNumber().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний илгээх дансны дугаар дутуу байна");
            }
            if (dto.getToAccountNumber() == null || dto.getToAccountNumber().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний шилжүүлэх дансны дугаар дутуу байна");
            }
            if (dto.getCurrencyCode() == null || dto.getCurrencyCode().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний валютын код дутуу байна");
            }
            if (dto.getFromAccountNumber().equals(dto.getToAccountNumber())) {
                throw new GMTValidationException("Илгээх болон хүлээн авах данс ижил байж болохгүй");
            }

            if (dto.getAmount().compareTo(100000000.0) > 0) {
                throw new GMTValidationException("Гүйлгээний дүн хязгаараас хэтэрсэн байна");
            }
            if (!GMTHelper.isValidAccountFormat(dto.getFromAccountNumber()) || !GMTHelper.isValidAccountFormat(dto.getToAccountNumber())) {
                throw new GMTValidationException("Дансны дугаарын формат буруу байна");
            }
            if (!GMTHelper.isSupportedCurrency(dto.getCurrencyCode())) {
                throw new GMTValidationException("Дэмжигдээгүй валют: " + dto.getCurrencyCode());
            }
        } catch (GMTValidationException e) {
            throw e;
        }
    }
    public static void validateTransactionReq(GMTDepositRequestDTO dto) throws GMTValidationException {
        try {
            if (dto == null) {
                throw new GMTValidationException("Гүйлгээний өгөгдөл дутуу байна");
            }
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                throw new GMTValidationException("Гүйлгээний мөнгөн дүн дутуу байна");
            }
            if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний шилжүүлэх дансны дугаар дутуу байна");
            }
            if (dto.getCurrencyCode() == null || dto.getCurrencyCode().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний валютын код дутуу байна");
            }

            if (dto.getAmount().compareTo(100000000.0) > 0) {
                throw new GMTValidationException("Гүйлгээний дүн хязгаараас хэтэрсэн байна");
            }
            if (!GMTHelper.isValidAccountFormat(dto.getAccountNumber())) {
                throw new GMTValidationException("Дансны дугаарын формат буруу байна");
            }
            if (!GMTHelper.isSupportedCurrency(dto.getCurrencyCode())) {
                throw new GMTValidationException("Дэмжигдээгүй валют: " + dto.getCurrencyCode());
            }
        } catch (GMTValidationException e) {
            throw e;
        }
    }


    public static void validateTransactionReq(GMTWithdrawalRequestDTO dto) throws GMTValidationException {
        try {
            if (dto == null) {
                throw new GMTValidationException("Гүйлгээний өгөгдөл дутуу байна");
            }
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                throw new GMTValidationException("Гүйлгээний мөнгөн дүн дутуу байна");
            }
            if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний шилжүүлэх дансны дугаар дутуу байна");
            }
            if (dto.getCurrencyCode() == null || dto.getCurrencyCode().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний валютын код дутуу байна");
            }

            if (dto.getAmount().compareTo(100000000.0) > 0) {
                throw new GMTValidationException("Гүйлгээний дүн хязгаараас хэтэрсэн байна");
            }
            if (!GMTHelper.isValidAccountFormat(dto.getAccountNumber())) {
                throw new GMTValidationException("Дансны дугаарын формат буруу байна");
            }
            if (!GMTHelper.isSupportedCurrency(dto.getCurrencyCode())) {
                throw new GMTValidationException("Дэмжигдээгүй валют: " + dto.getCurrencyCode());
            }
        } catch (GMTValidationException e) {
            throw e;
        }
    }

    public static void validateInterBankTransactionReq(GMTInterBankTransactionRequestDTO dto) throws GMTValidationException {
        try {
            if (dto == null) {
                throw new GMTValidationException("Банк хоорондын гүйлгээний өгөгдөл дутуу байна");
            }
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                throw new GMTValidationException("Гүйлгээний мөнгөн дүн дутуу байна");
            }
            if (dto.getFromAccountNumber() == null || dto.getFromAccountNumber().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний илгээх дансны дугаар дутуу байна");
            }
            if (dto.getToAccountNumber() == null || dto.getToAccountNumber().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний шилжүүлэх дансны дугаар дутуу байна");
            }
            if (dto.getCurrencyCode() == null || dto.getCurrencyCode().trim().isEmpty()) {
                throw new GMTValidationException("Гүйлгээний валютын код дутуу байна");
            }
            if (dto.getFromAccountNumber().equals(dto.getToAccountNumber())) {
                throw new GMTValidationException("Илгээх болон хүлээн авах данс ижил байж болохгүй");
            }

            if (dto.getAmount().compareTo(100000000.0) > 0) {
                throw new GMTValidationException("Гүйлгээний дүн хязгаараас хэтэрсэн байна");
            }
            if (!GMTHelper.isValidAccountFormat(dto.getFromAccountNumber())) {
                throw new GMTValidationException("Илгээх дансны дугаарын формат буруу байна");
            }
            if (!GMTHelper.isSupportedCurrency(dto.getCurrencyCode())) {
                throw new GMTValidationException("Дэмжигдээгүй валют: " + dto.getCurrencyCode());
            }

            if (dto.getDestinationBankCode() == null || dto.getDestinationBankCode().trim().isEmpty()) {
                throw new GMTValidationException("Хаяж буй банкны код дутуу байна");
            }
            if (dto.getDestinationBankName() == null || dto.getDestinationBankName().trim().isEmpty()) {
                throw new GMTValidationException("Хаяж буй банкны нэр дутуу байна");
            }
            if (dto.getSwiftCode() == null || dto.getSwiftCode().trim().isEmpty()) {
                throw new GMTValidationException("SWIFT код дутуу байна");
            }

            if (!dto.getSwiftCode().matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
                throw new GMTValidationException("SWIFT кодын формат буруу байна");
            }

        } catch (GMTValidationException e) {
            throw e;
        }
    }
    public static String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN" + timestamp + uuid;
    }

    public static Double calculateTransferFee(Double amount) {
        // 1.0 hoorond 100.0
        Double fee = amount * 0.001;
        return Math.max(1.0, Math.min(fee, 100.0));
    }

    public static Double calculateWithdrawalFee(Double amount) {
        return 5.0;
    }

    public static Double calculateInterBankTransferFee(Double amount, String currencyCode) {
        Double baseFee = 25.0;

        if (!GMTCurrencyConstant.MNT.value().equals(currencyCode)) {
            baseFee += 15.0;
        }


        if (amount > 10000000.0) {
            baseFee += amount * 0.0005;
        }
        
        return baseFee;
    }


    public static Long resolveAccountIdByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTCustomException {
        try {
            String base = "http://localhost:8085/api/accounts";
            HttpHeaders headers = new HttpHeaders();
            String auth = req.getHeader("Authorization");
            if (auth != null) headers.set("Authorization", auth);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            String url = base + "/account-number/" + accountNumber;
            System.out.println("DEBUG: Calling URL: " + url);
            
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new GMTCustomException("HTTP статус алдаа: " + resp.getStatusCode() + " - " + resp.getStatusCode().getReasonPhrase());
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
                throw (GMTCustomException) e;
            }
            throw new GMTCustomException("Дансны ID олоход алдаа гарсан: " + e.getMessage());
        }
    }
    public static boolean resolveAccountActiveByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTCustomException {
        String base = "http://localhost:8085/api/accounts";
        HttpHeaders headers = new HttpHeaders();
        String auth = req.getHeader("Authorization");
        if (auth != null) headers.set("Authorization", auth);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> resp = restTemplate.exchange(
                base + "/account-number/" + accountNumber,
                HttpMethod.GET,
                entity,
                Map.class
        );
        Map<?, ?> wrapper = resp.getBody();
        if (wrapper == null) throw new GMTCustomException("Хариу хоосон байна");
        Object body = wrapper.get("body");
        if (!(body instanceof Map)) throw new GMTCustomException("body буруу байна");
        Object active = ((Map<?, ?>) body).get("active");
        if (active instanceof Boolean) return ((Boolean)active).booleanValue();
        throw new GMTCustomException("active олдсонгүй");
    }

    public static void adjustAccountBalanceById(Long accountId, double amountDelta, HttpServletRequest req) {
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
        restTemplate.exchange(base + "/" + accountId + "/balance", HttpMethod.PUT, entity, Void.class);
    }

    public static double getAccountBalanceByAccountNumber(String accountNumber, HttpServletRequest req) throws GMTCustomException {
        String base = "http://localhost:8085/api/accounts";
        HttpHeaders headers = new HttpHeaders();
        String auth = req.getHeader("Authorization");
        if (auth != null) headers.set("Authorization", auth);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> resp = restTemplate.exchange(
                base + "/account-number/" + accountNumber,
                HttpMethod.GET,
                entity,
                Map.class
        );
        Map<?, ?> wrapper = resp.getBody();
        if (wrapper == null) throw new GMTCustomException("Хариу хоосон байна");
        Object body = wrapper.get("body");
        if (!(body instanceof Map)) throw new GMTCustomException("Body буруу байна");
        Object balanceVal = ((Map<?, ?>) body).get("balance");
        if (balanceVal instanceof Number) return ((Number) balanceVal).doubleValue();
        if (balanceVal instanceof String) return Double.parseDouble((String) balanceVal);
        throw new GMTCustomException("Үлдэгдэл олдсонгүй");
    }

    public static byte[] exportReport(GMTTransactionEntity transaction, String fromUser) throws GMTCustomException {
        try {
            if (transaction == null) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй: " + transaction);
            }

            StringBuilder report = new StringBuilder();
            report.append("Transaction List - ").append(fromUser).append("\n");
            report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            report.append("=".repeat(80)).append("\n");
            report.append(String.format("%-12s %-20s %-25s %-15s %-10s\n", "Date", "Transaction ID", "From → To", "Amount", "Status"));
            report.append("-".repeat(80)).append("\n");

                report.append(String.format("%-12s %-20s %-25s %-15s %-10s\n",
                        transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        transaction.getTransactionId(),
                        transaction.getFromAccountNumber() + " → " + transaction.getToAccountNumber(),
                        String.format("%.2f %s", transaction.getAmount(), transaction.getCurrencyCode()),
                        transaction.getStatus()));


            return report.toString().getBytes("UTF-8");

        } catch (Exception e) {
            throw new GMTCustomException("Тайлан гаргахад алдаа гарсан:\n " + e.getMessage());
        }
    }

    public static byte[] exportReport(List<GMTTransactionEntity> transactions, String fromUser) throws GMTCustomException {
        try {
            if (transactions == null || transactions.isEmpty()) {
                throw new GMTCustomException("Гүйлгээ олдсонгүй:");
            }
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDFont font = PDType1Font.TIMES_ROMAN;
            contentStream.beginText();
            contentStream.setFont(font, 14);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Transaction List ");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            contentStream.endText();
            
            int yPosition = 700;
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Date");
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText("From -> To");
            contentStream.newLineAtOffset(120, 0);
            contentStream.showText("Amount");
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText("Status");
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();
            
            yPosition -= 30;
            for (GMTTransactionEntity transaction : transactions) {
                if (yPosition < 50) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(font, 10);
                    yPosition = 750;
                }
                
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText(transaction.getFromAccountNumber() + " -> " + transaction.getToAccountNumber());
                contentStream.newLineAtOffset(120, 0);
                contentStream.showText(String.format("%.2f %s", transaction.getAmount(), transaction.getCurrencyCode()));
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText(transaction.getStatus());
                contentStream.endText();
                
                yPosition -= 20;
            }
            
            contentStream.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("PDF гаргахад алдаа гарсан: " + e.getMessage(), e);
        }
    }
    

    

}
