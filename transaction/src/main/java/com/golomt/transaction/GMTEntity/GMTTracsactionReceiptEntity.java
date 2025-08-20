package com.golomt.transaction.GMTEntity;

import java.util.Date;

public class GMTTracsactionReceiptEntity {
    private String payerAccountName;
    private String payerAccountNumber;
    private String payeeAccountIBAN;

    private String benefAccountNumber;
    private String benefAccountIBAN;
    private String benefAccountName;
    private String benefBankName;

    private Date transactionDate;
    private String transactionType;
    private String transactionStatus;
    private String transactionAmount;
    private String transactionStatusDesc;
    private String transactionId;
}
