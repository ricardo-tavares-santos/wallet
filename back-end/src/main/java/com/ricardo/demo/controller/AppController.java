package com.ricardo.demo.controller;

import com.ricardo.demo.dto.IdempotencyTokenDto;
import com.ricardo.demo.dto.TransactionDto;
import com.ricardo.demo.dto.TransactionListDto;
import com.ricardo.demo.dto.WalletDto;
import com.ricardo.demo.model.Player;
import com.ricardo.demo.model.Transaction;
import com.ricardo.demo.model.Wallet;
import com.ricardo.demo.repository.PlayerRepository;
import com.ricardo.demo.repository.TransactionRepository;
import com.ricardo.demo.repository.WalletRepository;
import com.ricardo.demo.service.LogicService;
import com.ricardo.demo.service.idempotency.TokenService;
import com.ricardo.demo.service.security.TokenProvider;
import com.ricardo.demo.type.TypeTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin //@CrossOrigin(origins = "http://localhost:8081")
@RestController

public class AppController {

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	LogicService logicService;

	// ------------- auth --------------

	private String userNotFoundEncodedPassword = "7777777777777777777777777 because i can ";

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/authenticate")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void authenticate() {
	}	
	@PostMapping("/login")
	public ResponseEntity<String> authorize(@Valid @RequestBody Player loginUser) {
		try {
			List<Player> players = new ArrayList<Player>();
			if (loginUser.getEmail() == null) {
				this.passwordEncoder.matches(loginUser.getPassword(), this.userNotFoundEncodedPassword);
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); 
			} else {
				playerRepository.findByEmail(loginUser.getEmail()).forEach(players::add);
			}
			if (players.isEmpty()) {
				this.passwordEncoder.matches(loginUser.getPassword(), this.userNotFoundEncodedPassword);
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			boolean pwMatches = this.passwordEncoder.matches(loginUser.getPassword(), players.get(0).getPassword());
			if (pwMatches) {
				String token = this.tokenProvider.createToken(players.get(0));
				return new ResponseEntity<>(token, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@PostMapping("/signup")
	public String signup(@RequestBody Player signupUser) {
		List<Player> players = new ArrayList<Player>();	
		playerRepository.findByEmail(signupUser.getEmail()).forEach(players::add);			
		if (!players.isEmpty()) {
			return "EXISTS";
		}
		signupUser.setPassword(passwordEncoder.encode(signupUser.getPassword()));
		playerRepository.save(new Player(signupUser.getName(), signupUser.getAdmin(), signupUser.getEmail(), signupUser.getPassword()));
		return this.tokenProvider.createToken(signupUser);
	}


    // ------------- wallet --------------
	// POST not allow values less than €0

	@PostMapping("/deposit")
	public ResponseEntity<WalletDto> createDeposit(@RequestBody TransactionDto lTransactionDto) {
		try {
			// verify idempotency
			if (lTransactionDto.getIdempotency_Key() == null || lTransactionDto.getIdempotency_Key() =="") {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			} else {
				String tokenMsg = tokenService.checkToken(lTransactionDto.getIdempotency_Key());
				if(tokenMsg!="OK") {
					return new ResponseEntity<>(null, HttpStatus.CONFLICT);
				}
			}

			// verify precontition
			if (Long.parseLong(lTransactionDto.getAmount())<0) {
				return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
			}

			logicService.saveTransaction(lTransactionDto, TypeTransaction.DEPOSIT);
			WalletDto lWalletDto = logicService.createDeposit(lTransactionDto);
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/deposit : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/withdraw")
	public ResponseEntity<WalletDto> createWithdraw(@RequestBody TransactionDto lTransactionDto) {
		try {
			// verify idempotency
			if (lTransactionDto.getIdempotency_Key() == null || lTransactionDto.getIdempotency_Key() =="") {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			} else {
				String tokenMsg = tokenService.checkToken(lTransactionDto.getIdempotency_Key());
				if(tokenMsg!="OK") {
					return new ResponseEntity<>(null, HttpStatus.CONFLICT);
				}
			}

			// verify precontition
			if (Long.parseLong(lTransactionDto.getAmount())<0 || !logicService.isWithdrawValid(lTransactionDto)) {
				return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
			}

			logicService.saveTransaction(lTransactionDto, TypeTransaction.WITHDRAW);
			WalletDto lWalletDto = logicService.createWithdraw(lTransactionDto);
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/withdraw : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// bet not allow values less than €0
	@PostMapping("/bet")
	public ResponseEntity<WalletDto> createBet(@RequestBody TransactionDto lTransactionDto) {
		try {
			// verify idempotency
			if (lTransactionDto.getIdempotency_Key() == null || lTransactionDto.getIdempotency_Key() =="") {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			} else {
				String tokenMsg = tokenService.checkToken(lTransactionDto.getIdempotency_Key());
				if(tokenMsg!="OK") {
					return new ResponseEntity<>(null, HttpStatus.CONFLICT);
				}
			}

			// verify precontition
			if (Long.parseLong(lTransactionDto.getAmount())<=0 || !logicService.isBetValid(lTransactionDto)) {
				return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
			}

			logicService.saveTransaction(lTransactionDto, TypeTransaction.BET);
			WalletDto lWalletDto = logicService.createBet(lTransactionDto);
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/bet : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/win")
	public ResponseEntity<WalletDto> createWin(@RequestBody TransactionDto lTransactionDto) {
		try {
			// verify idempotency
			if (lTransactionDto.getIdempotency_Key() == null || lTransactionDto.getIdempotency_Key() =="") {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			} else {
				String tokenMsg = tokenService.checkToken(lTransactionDto.getIdempotency_Key());
				if(tokenMsg!="OK") {
					return new ResponseEntity<>(null, HttpStatus.CONFLICT);
				}
			}

			// verify precontition
			if (Long.parseLong(lTransactionDto.getAmount())<0) {
				return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
			}

			logicService.saveTransaction(lTransactionDto, TypeTransaction.WIN);
			WalletDto lWalletDto = logicService.createWin(lTransactionDto);
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/win : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/transactions/{playerId}")
	public ResponseEntity<List<TransactionListDto>> getTransactions(@PathVariable("playerId") long playerId) {
		try {
			List<Transaction> lTransactions = new ArrayList<Transaction>();
			Sort sort = Sort.by(Sort.Direction.DESC, "dateTransaction");
			if (playerId == 0)
				transactionRepository.findAll(sort).forEach(lTransactions::add);
			else
				transactionRepository.findByPlayerId(playerId, sort).forEach(lTransactions::add);

			if (lTransactions.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			List<TransactionListDto> lTransactionListDto = new ArrayList<TransactionListDto>();
			for (Transaction t : lTransactions) {
				lTransactionListDto.add(logicService.convertTransactionListDto(t));
			}
			return new ResponseEntity<>(lTransactionListDto, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("/transactions : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/balance/{playerId}")
	public ResponseEntity<WalletDto> getBalance(@PathVariable("playerId") long playerId) {
		try {
			Wallet lWallet = new Wallet();
			if (playerId > 0)
				lWallet = walletRepository.findByPlayerId(playerId);
			if (lWallet == null) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(logicService.convertWalletDto(lWallet), HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("/balance : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ------------- idempotency --------------

	@Autowired
	private TokenService tokenService;

	@GetMapping("/getIdempotencyToken")
	public ResponseEntity<IdempotencyTokenDto> getIdempotencyToken() {
		try {
			String idempotencyToken = tokenService.createToken();
			if (idempotencyToken == null || idempotencyToken=="") {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			}
			IdempotencyTokenDto lIdempotencyTokenDto = new IdempotencyTokenDto();
			lIdempotencyTokenDto.setIdempotency_Key(idempotencyToken);
			return new ResponseEntity<>(lIdempotencyTokenDto, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("/balance : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}