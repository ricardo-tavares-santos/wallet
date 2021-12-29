package com.ricardo.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Date;
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

	private static String email = "application@test.com";
	private static PlayerEntity player = new PlayerEntity("name", false, email, "password");

	// -------- Integration controller test

	@Test
	@Order(1)
	public void should_Controllers_Works() throws Exception {
		playerRepository.save(player);
		List<PlayerEntity> players = playerRepository.findByEmail(email);
		long playerId = players.stream().findFirst().get().getId();
		assertNotNull(players.size()>0?true:null);

		String token = this.tokenProvider.createToken(player);
		assertNotNull(token);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		TransactionDto lTransactionSaveDto = new TransactionDto();
		lTransactionSaveDto.setDateTransaction(new Date()+"");
		lTransactionSaveDto.setPlayerId(playerId+"");

		//  POST /deposit
		lTransactionSaveDto.setTransactionId(UUID.randomUUID().toString());
		lTransactionSaveDto.setTypeTransaction(TransactionType.DEPOSIT.name());
		lTransactionSaveDto.setAmount("10");
		String requestDepositJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
						.post("/deposit")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestDepositJson))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.cashBalance").value("10"));

		//  POST /bet
		lTransactionSaveDto.setTransactionId(UUID.randomUUID().toString());
		lTransactionSaveDto.setTypeTransaction(TransactionType.BET.name());
		lTransactionSaveDto.setAmount("10");
		lTransactionSaveDto.setBonusBet("0");
		lTransactionSaveDto.setCashBet("10");
		String requestBetJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
						.post("/bet")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBetJson))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.cashBalance").value("0"));
		lTransactionSaveDto.setBonusBet(null);
		lTransactionSaveDto.setCashBet(null);

		//  POST /win
		lTransactionSaveDto.setTypeTransaction(TransactionType.WIN.name());
		lTransactionSaveDto.setAmount("20");
		String requestWinJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
						.post("/win")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestWinJson))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.cashBalance").value("20"));

		//  GET /balance/{playerId}
		mvc.perform(MockMvcRequestBuilders.get("/balance/"+playerId)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.cashBalance").value("20"));

		//  POST /withdraw ok
		lTransactionSaveDto.setTransactionId(UUID.randomUUID().toString());
		lTransactionSaveDto.setTypeTransaction(TransactionType.WITHDRAW.name());
		lTransactionSaveDto.setAmount("20");
		String requestWithdrawOKJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
						.post("/withdraw")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestWithdrawOKJson))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.cashBalance").value("0"));

		//  POST /withdraw idempotent
		lTransactionSaveDto.setTypeTransaction(TransactionType.WITHDRAW.name());
		lTransactionSaveDto.setAmount("20");
		String requestWithdrawIdempotentJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
						.post("/withdraw")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestWithdrawIdempotentJson))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.cashBalance").value("0"));

		//  POST /withdraw conflict
		lTransactionSaveDto.setTypeTransaction(TransactionType.WITHDRAW.name());
		lTransactionSaveDto.setAmount("1000");
		String requestWithdrawConflictJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
						.post("/withdraw")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestWithdrawConflictJson))
				.andExpect(status().isConflict());


		//  GET /transactions/{playerId}
		mvc.perform(MockMvcRequestBuilders.get("/transactions/"+playerId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(4));
	}

	// -------- Logic test

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
