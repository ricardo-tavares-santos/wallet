package com.ricardo.demo.dto;

import javax.persistence.Column;

public class TransactionDto {
    private String transactionId;
    private String playerId;
    private String amount;
    private String dateTransaction;
    private String typeTransaction;
    private String cashBet;
    private String bonusBet;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(String dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public String getTypeTransaction() {return typeTransaction; }

    public void setTypeTransaction(String typeTransaction) {this.typeTransaction = typeTransaction;}

    public String getCashBet() {
        return cashBet;
    }

    public void setCashBet(String cashBet) {
        this.cashBet = cashBet;
    }

    public String getBonusBet() {
        return bonusBet;
    }

    public void setBonusBet(String bonusBet) {
        this.bonusBet = bonusBet;
    }
}
