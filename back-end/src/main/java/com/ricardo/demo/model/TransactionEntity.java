package com.ricardo.demo.model;

import javax.persistence.*;
import com.ricardo.demo.type.TransactionType;

import java.util.Date;

@Entity
@Table(name = "transaction")
public class TransactionEntity {
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
    private TransactionType typeTransaction;

	public TransactionEntity() {
	}

	public TransactionEntity(long playerId, String transactionId, long amount, Date dateTransaction, TransactionType typeTransaction) {
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

	public TransactionType getTypeTransaction() {
		return typeTransaction;
	}
	public void setTypeTransaction(TransactionType typeTransaction) {
		this.typeTransaction = typeTransaction;
	}


	@Override
	public String toString() {
		return "Transaction [id=" + id + ", transactionId=" + transactionId + ", amount=" + amount + ", dateTransaction=" + dateTransaction + ", typeTransaction=" + typeTransaction + "]";
	}
}
