package com.golomt.auth.GMTUtility;

import java.util.UUID;

public class GMTAuthUtilities {
    public static String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    public static String generateCustomerId() {
        return "CUST" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
    }
}
