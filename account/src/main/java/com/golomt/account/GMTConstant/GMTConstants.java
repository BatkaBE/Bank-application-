package com.golomt.account.GMTConstant;

import com.golomt.account.GMTConstant.GMTCMNConstant;

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
    interface CONTRACT {
        interface TYPE {
            String SYSTEM = "SYSTEM";

        }
    }
}