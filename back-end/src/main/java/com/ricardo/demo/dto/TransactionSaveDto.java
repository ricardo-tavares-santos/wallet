package com.ricardo.demo.dto;

public class TransactionSaveDto {
    private String idempotency_Key;
    private TransactionDto data;

    public String getIdempotency_Key() {
        return idempotency_Key;
    }

    public void setIdempotency_Key(String idempotency_Key) {
        this.idempotency_Key = idempotency_Key;
    }

    public TransactionDto getData() {
        return data;
    }

    public void setData(TransactionDto data) {
        this.data = data;
    }
}
