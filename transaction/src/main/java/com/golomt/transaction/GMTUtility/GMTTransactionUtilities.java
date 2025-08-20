package com.golomt.transaction.GMTUtility;

import java.util.UUID;

public class GMTTransactionUtilities {
    public static String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
