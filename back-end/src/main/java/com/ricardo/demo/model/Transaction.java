package com.ricardo.demo.model;

import javax.persistence.*;
import com.ricardo.demo.type.TypeTransaction;

import java.util.Date;

@Entity
@Table(name = "transaction")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

    @Column(name = "playerId")
	private long playerId;

	@Column(name = "transactionId")
	private String transactionId;

	@Column(name = "amount")
	private long amount;

	@Column(name = "dateTransaction")
	private Date dateTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeTransaction")
    private TypeTransaction typeTransaction;

	public Transaction() {
	}

	public Transaction(long playerId, String transactionId, long amount, Date dateTransaction, TypeTransaction typeTransaction) {
		this.playerId = playerId;
		this.transactionId = transactionId;
		this.amount = amount;
		this.dateTransaction = dateTransaction;
		this.typeTransaction = typeTransaction;
	}

	public long getId() {
		return id;
	}

	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}

	public Date getDateTransaction() {
		return dateTransaction;
	}
	public void setDateTransaction(Date dateTransaction) {
		this.dateTransaction = dateTransaction;
	}

	public TypeTransaction getTypeTransaction() {
		return typeTransaction;
	}
	public void setTypeTransaction(TypeTransaction typeTransaction) {
		this.typeTransaction = typeTransaction;
	}


	@Override
	public String toString() {
		return "Transaction [id=" + id + ", transactionId=" + transactionId + ", amount=" + amount + ", dateTransaction=" + dateTransaction + ", typeTransaction=" + typeTransaction + "]";
	}
}
