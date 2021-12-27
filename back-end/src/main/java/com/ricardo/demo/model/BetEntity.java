package com.ricardo.demo.model;

import javax.persistence.*;

@Entity
@Table(name = "bet")
public class BetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "playerId")
    private long playerId;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "cashBet")
    private long cashBet;

    @Column(name = "bonusBet")
    private long bonusBet;

    public long getPlayerId() {
        return playerId;
    }
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getCashBet() {
        return cashBet;
    }

    public void setCashBet(long cashBet) {
        this.cashBet = cashBet;
    }

    public long getBonusBet() {
        return bonusBet;
    }

    public void setBonusBet(long bonusBet) {
        this.bonusBet = bonusBet;
    }

    public BetEntity() {
    }

    public BetEntity(long playerId, String transactionId, long cashBet, long bonusBet) {
        this.playerId = playerId;
        this.transactionId = transactionId;
        this.cashBet = cashBet;
        this.bonusBet = bonusBet;
    }

    @Override
    public String toString() {
        return "Bet [id=" + id + ", transactionId=" + transactionId + ", cashBet=" + cashBet + ", bonusBet=" + bonusBet + "]";
    }
}
