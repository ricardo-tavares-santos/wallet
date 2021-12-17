package com.ricardo.demo.service;

import com.ricardo.demo.dto.TransactionDto;
import com.ricardo.demo.dto.WalletDto;
import com.ricardo.demo.model.Transaction;
import com.ricardo.demo.model.Wallet;
import com.ricardo.demo.repository.PlayerRepository;
import com.ricardo.demo.repository.TransactionRepository;
import com.ricardo.demo.repository.WalletRepository;
import com.ricardo.demo.type.TypeTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@ConfigurationProperties(prefix = "app")
@Service
public class LogicService {

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	WalletRepository walletRepository;

	// only cash balances can be withdrawn
	public WalletDto createWithdraw(TransactionDto lTransactionDto) {
		Wallet lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount());
		lWallet.setCashBalance(lWallet.getCashBalance()-amount);
		walletRepository.save(lWallet);
		return convertWalletDto(lWallet);
	}
	public boolean isWithdrawValid(TransactionDto lTransactionDto) {
		Wallet lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long totalValue = lWallet.getCashBalance();
		if (totalValue >= Long.parseLong(lTransactionDto.getAmount())) {
			return true;
		}
		return false;
	}

	// money withdrawn for bets takes a cash first approach
	public WalletDto createBet(TransactionDto lTransactionDto) {
		Wallet lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount());
		long cash = lWallet.getCashBalance();
		if(cash-amount < 0) {
			lWallet.setBonusBalance(lWallet.getBonusBalance()+cash-amount);
			lWallet.setCashBalance(0);
		} else {
			lWallet.setCashBalance(lWallet.getCashBalance()-amount);
		}
		walletRepository.save(lWallet);
		return convertWalletDto(lWallet);
	}
	public boolean isBetValid(TransactionDto lTransactionDto) {
		Wallet lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long totalValue = lWallet.getBonusBalance()+lWallet.getCashBalance();
		if (totalValue >= Long.parseLong(lTransactionDto.getAmount())) {
			return true;
		}
		return false;
	}

	// wins from bets must be always split proportionally to bet
	public WalletDto createWin(TransactionDto lTransactionDto) {
		Wallet lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount())/2;
		lWallet.setBonusBalance(lWallet.getBonusBalance()+amount);
		lWallet.setCashBalance(lWallet.getCashBalance()+amount);
		walletRepository.save(lWallet);
		return convertWalletDto(lWallet);
	}

	// 100% bonus for any deposit greater than €100
	public WalletDto createDeposit(TransactionDto lTransactionDto) {
		Wallet lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		Wallet pWallet = new Wallet();
		pWallet.setPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount());
		if (lWallet==null) {
			pWallet.setCashBalance(amount);
			if (amount>=100) {
				pWallet.setBonusBalance(amount);
			}
			walletRepository.save(pWallet);
		} else {
			pWallet.setCashBalance(lWallet.getCashBalance()+amount);
			lWallet.setCashBalance(lWallet.getCashBalance()+amount);
			if (amount>=100) {
				pWallet.setBonusBalance(lWallet.getBonusBalance()+amount);
				lWallet.setBonusBalance(lWallet.getBonusBalance()+amount);
			}
			walletRepository.save(lWallet);
		}
		return convertWalletDto(pWallet);
	}

	public void saveTransaction(TransactionDto lTransactionDto, TypeTransaction lTypeTransaction) {
		Transaction lTransaction = new Transaction();
		lTransaction.setDateTransaction(new Date());
		lTransaction.setTypeTransaction(lTypeTransaction);
		lTransaction.setTransactionId(lTransactionDto.getTransactionId());
		lTransaction.setPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		lTransaction.setAmount(Long.parseLong(lTransactionDto.getAmount()));
		transactionRepository.save(lTransaction);
	}

	public WalletDto convertWalletDto (Wallet lWallet ) {
		WalletDto lWalletDto = new WalletDto();
		lWalletDto.setCashBalance(lWallet.getCashBalance()+"");
		lWalletDto.setBonusBalance(lWallet.getBonusBalance()+"");
		return lWalletDto;
	}

	public TransactionDto convertTransactionDto (Transaction lTransaction) {
		TransactionDto lTransactiontDto = new TransactionDto();
		lTransactiontDto.setTransactionId(lTransaction.getTransactionId());
		String dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lTransaction.getDateTransaction());
		lTransactiontDto.setDateTransaction(dataFormat);
		lTransactiontDto.setAmount(lTransaction.getAmount()+"");
		lTransactiontDto.setPlayerId(lTransaction.getPlayerId()+"");
		lTransactiontDto.setTypeTransaction(lTransaction.getTypeTransaction().name());
		return lTransactiontDto;
	}

}