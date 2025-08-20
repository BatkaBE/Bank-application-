package com.golomt.transaction.GMTConstant;

import com.golomt.transaction.GMTConstant.GMTCMNConstant;

public interface GMTConstants extends GMTCMNConstant {
    interface ACCOUNT {
        interface TYPE {
            String OPR = "OPR";
            String DEP = "DEP";
            String LON = "LON";
            String CCD = "CCD";
            String TUA = "TUA";
            String TDA = "TDA";
        }
    }

    interface TRANSACTION {
        interface TYPE {
            String TRANSFER = "TRANSFER";
            String DEPOSIT = "DEPOSIT";
            String WITHDRAWAL = "WITHDRAWAL";
            String PAYMENT = "PAYMENT";
            String REFUND = "REFUND";
        }
    }

    interface ENCRYPT {
        String CIPHER_INSTANCE = "AES/CBC/PKCS5PADDING";
        String AES = "AES";

    }
}