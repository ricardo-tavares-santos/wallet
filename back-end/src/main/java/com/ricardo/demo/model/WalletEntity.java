package com.ricardo.demo.model;

import javax.persistence.*;

@Entity
@Table(name = "wallet")
public class WalletEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "playerId")
	private long playerId;

	@Column(name = "cashBalance")
	private long cashBalance;

	@Column(name = "bonusBalance")
	private long bonusBalance;

	public WalletEntity() {

	}

	public WalletEntity(long playerId, long cashBalance, long bonusBalance) {
		this.playerId = playerId;
		this.cashBalance = cashBalance;
		this.bonusBalance = bonusBalance;
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

	public long getCashBalance() {
		return cashBalance;
	}
	public void setCashBalance(long cashBalance) {
		this.cashBalance = cashBalance;
	}

	public long getBonusBalance() {
		return bonusBalance;
	}
	public void setBonusBalance(long bonusBalance) {
		this.bonusBalance = bonusBalance;
	}


	@Override
	public String toString() {
		return "Wallet [id=" + id + ", playerId=" + playerId + ", cashBalance=" + cashBalance + ", bonusBalance=" + bonusBalance + "]";
	}
}
