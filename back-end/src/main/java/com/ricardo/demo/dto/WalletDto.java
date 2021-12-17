package com.ricardo.demo.dto;

public class WalletDto {
    private String cashBalance;
    private String bonusBalance;

    public String getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(String cashBalance) {
        this.cashBalance = cashBalance;
    }

    public String getBonusBalance() {
        return bonusBalance;
    }

    public void setBonusBalance(String bonusBalance) {
        this.bonusBalance = bonusBalance;
    }
}
