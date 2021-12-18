package com.ricardo.demo.dto;

public class IdempotencyTokenDto {
    private String Idempotency_Key;
    public String getIdempotency_Key() {return Idempotency_Key; }
    public void setIdempotency_Key(String idempotency_Key) {Idempotency_Key = idempotency_Key;}
}
