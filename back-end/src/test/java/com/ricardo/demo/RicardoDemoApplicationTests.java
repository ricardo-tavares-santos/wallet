package com.ricardo.demo;

import com.ricardo.demo.dto.TransactionDto;
import com.ricardo.demo.model.PlayerEntity;
import com.ricardo.demo.model.WalletEntity;
import com.ricardo.demo.repository.BetRepository;
import com.ricardo.demo.repository.PlayerRepository;
import com.ricardo.demo.repository.TransactionRepository;
import com.ricardo.demo.repository.WalletRepository;
import com.ricardo.demo.service.LogicService;
import com.ricardo.demo.service.security.TokenProvider;
import com.ricardo.demo.type.TransactionType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RicardoDemoApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	BetRepository betRepository;

	@Autowired
	LogicService logicService;

	// -------- App test

	private static String email = "application@test.com";
	private static PlayerEntity player = new PlayerEntity("name", false, email, "password");

	@Test
	@Order(1)
	public void shoud_Insert_Player() throws Exception {
		// if player ok
		playerRepository.save(player);
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
	}
	@Test
	@Order(2)
	public void shoud_Create_Deposit_Simple() throws Exception {
		// amount 50
		// if cash 50 ok
		// if bonus 0 ok
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
		long playerId = players.stream().findFirst().get().getId();

		TransactionDto lTransactionDto = new TransactionDto();
		lTransactionDto.setTypeTransaction(TransactionType.DEPOSIT.name());
		lTransactionDto.setAmount("50");
		lTransactionDto.setPlayerId(playerId+"");
		lTransactionDto.setTransactionId(UUID.randomUUID().toString());
		logicService.saveTransaction(lTransactionDto, TransactionType.DEPOSIT);
		logicService.createDeposit(lTransactionDto);

		WalletEntity lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 50);
		assertEquals(lWalletEntity.getBonusBalance(), 0);
	}
	@Test
	@Order(3)
	public void shoud_Create_Deposit_Bonus() throws Exception {
		// amount 100
		// if cash 150 ok
		// if bonus 100 ok
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
		long playerId = players.stream().findFirst().get().getId();

		TransactionDto lTransactionDto = new TransactionDto();
		lTransactionDto.setTypeTransaction(TransactionType.DEPOSIT.name());
		lTransactionDto.setAmount("100");
		lTransactionDto.setPlayerId(playerId+"");
		lTransactionDto.setTransactionId(UUID.randomUUID().toString());
		logicService.saveTransaction(lTransactionDto, TransactionType.DEPOSIT);
		logicService.createDeposit(lTransactionDto);

		WalletEntity lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 150);
		assertEquals(lWalletEntity.getBonusBalance(), 100);
	}
	@Test
	@Order(4)
	public void shoud_Create_Bet_Win_Cash_Bonus() throws Exception {
		// amount 250
		// if cash 0 ok
		// if bonus 0 ok
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
		long playerId = players.stream().findFirst().get().getId();

		TransactionDto lTransactionDto = new TransactionDto();
		lTransactionDto.setTypeTransaction(TransactionType.BET.name());
		lTransactionDto.setAmount("250");
		lTransactionDto.setCashBet("150");
		lTransactionDto.setBonusBet("100");
		lTransactionDto.setPlayerId(playerId+"");
		lTransactionDto.setTransactionId(UUID.randomUUID().toString());
		logicService.saveTransaction(lTransactionDto, TransactionType.BET);
		logicService.createBet(lTransactionDto);

		WalletEntity lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 0);
		assertEquals(lWalletEntity.getBonusBalance(), 0);

		// WIN last Bet (won 100%)
		// if cash 300 ok
		// if bonus 200 ok
		lTransactionDto.setTypeTransaction(TransactionType.WIN.name());
		lTransactionDto.setAmount("500");
		logicService.saveTransaction(lTransactionDto, TransactionType.WIN);
		logicService.createWin(lTransactionDto);

		lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 300);
		assertEquals(lWalletEntity.getBonusBalance(), 200);
	}
	@Test
	@Order(5)
	public void shoud_Withdraw_Cash_Idempotent() throws Exception {
		// amount 300
		// if cash 0 ok
		// if bonus 200 ok
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
		long playerId = players.stream().findFirst().get().getId();

		TransactionDto lTransactionDto = new TransactionDto();
		lTransactionDto.setTypeTransaction(TransactionType.WITHDRAW.name());
		lTransactionDto.setAmount("300");
		lTransactionDto.setPlayerId(playerId+"");
		lTransactionDto.setTransactionId(UUID.randomUUID().toString());

		// verify first idempotency
		String idempotency = logicService.verifyIdempotency(lTransactionDto);
		assertEquals(idempotency, "new");

		logicService.saveTransaction(lTransactionDto, TransactionType.WITHDRAW);
		logicService.createWithdraw(lTransactionDto);

		WalletEntity lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 0);
		assertEquals(lWalletEntity.getBonusBalance(), 200);

		// verify same idempotency
		// amount 300 (same transactionId/transactionType)
		idempotency = logicService.verifyIdempotency(lTransactionDto);
		assertEquals(idempotency, "same");

		// verify conflict idempotency
		// amount 1000 (same transactionId/transactionType)
		lTransactionDto.setAmount("1000");
		idempotency = logicService.verifyIdempotency(lTransactionDto);
		assertEquals(idempotency, "conflict");
	}
	@Test
	@Order(6)
	public void shoud_Create_Bet_Win_Bonus() throws Exception {
		// amount 200
		// if cash 0 ok
		// if bonus 0 ok
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
		long playerId = players.stream().findFirst().get().getId();

		TransactionDto lTransactionDto = new TransactionDto();
		lTransactionDto.setTypeTransaction(TransactionType.BET.name());
		lTransactionDto.setAmount("200");
		lTransactionDto.setCashBet("0");
		lTransactionDto.setBonusBet("200");
		lTransactionDto.setPlayerId(playerId+"");
		lTransactionDto.setTransactionId(UUID.randomUUID().toString());
		logicService.saveTransaction(lTransactionDto, TransactionType.BET);
		logicService.createBet(lTransactionDto);

		WalletEntity lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 0);
		assertEquals(lWalletEntity.getBonusBalance(), 0);

		// WIN last Bet (won 100%)
		// if cash 0 ok
		// if bonus 400 ok
		lTransactionDto.setTypeTransaction(TransactionType.WIN.name());
		lTransactionDto.setAmount("400");
		logicService.saveTransaction(lTransactionDto, TransactionType.WIN);
		logicService.createWin(lTransactionDto);

		lWalletEntity = walletRepository.findByPlayerId(playerId);
		assertEquals(lWalletEntity.getCashBalance(), 0);
		assertEquals(lWalletEntity.getBonusBalance(), 400);
	}
	@Test
	@Order(7)
	public void shoud_Delete_All() throws Exception {
		// if deleted ok
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);
		long playerId = players.stream().findFirst().get().getId();

		long betDelete = betRepository.deleteAllByPlayerId(playerId);
		assertNotNull(betDelete>0?true:null);

		long transactionDelete = transactionRepository.deleteAllByPlayerId(playerId);
		assertNotNull(transactionDelete>0?true:null);

		long walletDelete = walletRepository.deleteAllByPlayerId(playerId);
		assertNotNull(walletDelete>0?true:null);

		playerRepository.deleteById(playerId);
		players = playerRepository.findByEmail(email);
		assertNull(players.size()>0?true:null);
	}


	// -------- Security test

	@Test
	@Order(8)
	public void should_AllowAccess() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/public")).andExpect(status().isOk());
	}

	@Test
	@Order(9)
	public void should_NotAllowAccess() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/secret")).andExpect(status().isForbidden());
	}

	@Test
	@Order(10)
	public void should_NotAllowAccessToUnauthenticatedUsers() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/transactions/0")).andExpect(status().isForbidden());
	}

	@Test
	@Order(11)
	public void should_InsertUser_GenerateAuthToken_DeleteUser() throws Exception {
		playerRepository.save(player);
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		assertNotNull(players.size()>0?true:null);

		String token = this.tokenProvider.createToken(player);
		assertNotNull(token);
		mvc.perform(MockMvcRequestBuilders.get("/secret").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

		playerRepository.deleteById(players.stream().findFirst().get().getId());
		players = playerRepository.findByEmail(email);

		assertNull(players.size()>0?true:null);
	}

}
