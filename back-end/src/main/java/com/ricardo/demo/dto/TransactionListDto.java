package com.ricardo.demo.dto;

public class TransactionListDto {
    private String transactionId;
    private String playerId;
    private String amount;
    private String dateTransaction;
    private String typeTransaction;

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

}
