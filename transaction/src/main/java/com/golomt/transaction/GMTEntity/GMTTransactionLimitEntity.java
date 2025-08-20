package com.golomt.transaction.GMTEntity;

public class GMTTransactionLimitEntity {
    private String transactionType;
    private String dailyLimitApplicable;
    private String weeklyLimitApplicable;
    private String monthlyLimitApplicable;
    private String yearlyLimitApplicable;
    private GMTAmountEntity dailyLimitAvailable;
    private GMTAmountEntity weeklyLimitAvailable;
    private GMTAmountEntity monthlyLimitAvailable;
    private GMTAmountEntity yearlyLimitAvailable;
    private GMTAmountEntity totalDailyLimit;
    private GMTAmountEntity totalWeeklyLimit;
    private GMTAmountEntity totalMonthlyLimit;
    private GMTAmountEntity totalYearlyLimit;
}
