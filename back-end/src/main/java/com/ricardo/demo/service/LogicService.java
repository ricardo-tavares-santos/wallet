package com.ricardo.demo.service;

import com.ricardo.demo.dto.TransactionDto;
import com.ricardo.demo.dto.TransactionListDto;
import com.ricardo.demo.dto.WalletDto;
import com.ricardo.demo.model.TransactionEntity;
import com.ricardo.demo.model.WalletEntity;
import com.ricardo.demo.repository.TransactionRepository;
import com.ricardo.demo.repository.WalletRepository;
import com.ricardo.demo.type.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ConfigurationProperties(prefix = "app")
@Service
public class LogicService {

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	WalletRepository walletRepository;

	// only cash balances can be withdrawn
	public WalletDto createWithdraw(TransactionDto lTransactionDto) {
		WalletEntity lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount());
		lWallet.setCashBalance(lWallet.getCashBalance()-amount);
		walletRepository.save(lWallet);
		return convertWalletEntityToDto(lWallet);
	}
	public boolean isWithdrawValid(TransactionDto lTransactionDto) {
		WalletEntity lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long totalValue = lWallet.getCashBalance();
		if (totalValue >= Long.parseLong(lTransactionDto.getAmount())) {
			return true;
		}
		return false;
	}

	// money withdrawn for bets takes a cash first approach
	public WalletDto createBet(TransactionDto lTransactionDto) {
		WalletEntity lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount());
		long cash = lWallet.getCashBalance();
		if(cash-amount < 0) {
			lWallet.setBonusBalance(lWallet.getBonusBalance()+cash-amount);
			lWallet.setCashBalance(0);
		} else {
			lWallet.setCashBalance(lWallet.getCashBalance()-amount);
		}
		walletRepository.save(lWallet);
		return convertWalletEntityToDto(lWallet);
	}
	public boolean isBetValid(TransactionDto lTransactionDto) {
		WalletEntity lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long totalValue = lWallet.getBonusBalance()+lWallet.getCashBalance();
		if (totalValue >= Long.parseLong(lTransactionDto.getAmount())) {
			return true;
		}
		return false;
	}

	// wins from bets must be always split proportionally to bet
	public WalletDto createWin(TransactionDto lTransactionDto) {
		WalletEntity lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		long amount = Long.parseLong(lTransactionDto.getAmount())/2;
		lWallet.setBonusBalance(lWallet.getBonusBalance()+amount);
		lWallet.setCashBalance(lWallet.getCashBalance()+amount);
		walletRepository.save(lWallet);
		return convertWalletEntityToDto(lWallet);
	}

	// 100% bonus for any deposit greater than €100
	public WalletDto createDeposit(TransactionDto lTransactionDto) {
		WalletEntity lWallet = walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		WalletEntity pWallet = new WalletEntity();
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
		return convertWalletEntityToDto(pWallet);
	}

	public void saveTransaction(TransactionDto lTransactionDto, TransactionType lTypeTransaction) {
		TransactionEntity lTransaction = new TransactionEntity();
		lTransaction.setDateTransaction(new Date());
		lTransaction.setTypeTransaction(lTypeTransaction);
		lTransaction.setTransactionId(lTransactionDto.getTransactionId());
		lTransaction.setPlayerId(Long.parseLong(lTransactionDto.getPlayerId()));
		lTransaction.setAmount(Long.parseLong(lTransactionDto.getAmount()));
		transactionRepository.save(lTransaction);
	}

	public WalletDto convertWalletEntityToDto (WalletEntity lWallet ) {
		WalletDto lWalletDto = new WalletDto();
		lWalletDto.setCashBalance(lWallet.getCashBalance()+"");
		lWalletDto.setBonusBalance(lWallet.getBonusBalance()+"");
		return lWalletDto;
	}

	public TransactionDto convertTransactionEntityToDto (TransactionEntity lTransaction) {
		TransactionDto lTransactionDto = new TransactionDto();
		lTransactionDto.setTransactionId(lTransaction.getTransactionId());
		String dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lTransaction.getDateTransaction());
		lTransactionDto.setDateTransaction(dataFormat);
		lTransactionDto.setAmount(lTransaction.getAmount()+"");
		lTransactionDto.setPlayerId(lTransaction.getPlayerId()+"");
		lTransactionDto.setTypeTransaction(lTransaction.getTypeTransaction().name());
		return lTransactionDto;
	}

	public TransactionListDto findAllTransactions ( long playerId, Integer pageNo, Integer pageSize, String sortBy) {
		TransactionListDto lTransactionListDto = new TransactionListDto();
		Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
		List<TransactionEntity> lTransaction = new ArrayList<TransactionEntity>();
		Page<TransactionEntity> pagedResult;
		if (playerId == 0) {
			pagedResult = transactionRepository.findAll(paging);
		} else {
			pagedResult = transactionRepository.findByPlayerId(playerId, paging);
		}
		lTransactionListDto.setPageSizeTotal(pagedResult.getTotalElements());
		lTransactionListDto.setPageNoTotal(pagedResult.getTotalPages());
		if(pagedResult.hasContent()) {
			lTransaction = pagedResult.getContent();
		}
		List<TransactionDto> lTransactionsDto = new ArrayList<TransactionDto>();
		for (TransactionEntity t : lTransaction) {
			lTransactionsDto.add(convertTransactionEntityToDto(t));
		}
		lTransactionListDto.setData(lTransactionsDto);
		return lTransactionListDto;
	}

}