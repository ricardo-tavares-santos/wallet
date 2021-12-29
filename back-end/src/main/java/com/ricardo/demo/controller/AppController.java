package com.ricardo.demo.controller;

import com.ricardo.demo.dto.*;
import com.ricardo.demo.model.PlayerEntity;
import com.ricardo.demo.model.WalletEntity;
import com.ricardo.demo.repository.PlayerRepository;
import com.ricardo.demo.repository.WalletRepository;
import com.ricardo.demo.service.LogicService;
import com.ricardo.demo.service.security.TokenProvider;
import com.ricardo.demo.type.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
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
	public ResponseEntity<String> authorize(@Valid @RequestBody PlayerEntity loginUser) {
		try {
			List<PlayerEntity> players = new ArrayList<PlayerEntity>();
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
	public String signup(@RequestBody PlayerEntity signupUser) {
		List<PlayerEntity> players = new ArrayList<PlayerEntity>();
		playerRepository.findByEmail(signupUser.getEmail()).forEach(players::add);			
		if (!players.isEmpty()) {
			return "EXISTS";
		}
		signupUser.setPassword(passwordEncoder.encode(signupUser.getPassword()));
		playerRepository.save(new PlayerEntity(signupUser.getName(), signupUser.getAdmin(), signupUser.getEmail(), signupUser.getPassword()));
		return this.tokenProvider.createToken(signupUser);
	}


    // ------------- wallet --------------
	// POST not allow values less than €0

	@PostMapping("/deposit")
	public ResponseEntity<WalletDto> createDeposit(@RequestBody TransactionDto lTransactionDto) {
		try {
			WalletDto lWalletDto = new WalletDto();
			String idempotency = logicService.verifyIdempotency(lTransactionDto);
			switch (idempotency) {
				case "conflict":
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				case "same":
					lWalletDto = logicService.convertWalletEntityToDto(walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId())));
					break;
				case "new":
					// verify precontition
					if (Long.parseLong(lTransactionDto.getAmount())<0
							|| lTransactionDto.getTypeTransaction()==null
							|| lTransactionDto.getTransactionId()==null
					) {
						return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
					}
					logicService.saveTransaction(lTransactionDto, TransactionType.DEPOSIT);
					lWalletDto = logicService.createDeposit(lTransactionDto);
					break;
			}
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/deposit : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/withdraw")
	public ResponseEntity<WalletDto> createWithdraw(@RequestBody TransactionDto lTransactionDto) {
		try {
			WalletDto lWalletDto = new WalletDto();
			String idempotency = logicService.verifyIdempotency(lTransactionDto);
			switch (idempotency) {
				case "conflict":
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				case "same":
					lWalletDto = logicService.convertWalletEntityToDto(walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId())));
					break;
				case "new":
					// verify precontition
					if (Long.parseLong(lTransactionDto.getAmount())<0
							|| lTransactionDto.getTypeTransaction()==null
							|| lTransactionDto.getTransactionId()==null
							|| !logicService.isWithdrawValid(lTransactionDto)) {
						return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
					}
					logicService.saveTransaction(lTransactionDto, TransactionType.WITHDRAW);
					lWalletDto = logicService.createWithdraw(lTransactionDto);
					break;
			}
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
			WalletDto lWalletDto = new WalletDto();
			String idempotency = logicService.verifyIdempotency(lTransactionDto);
			switch (idempotency) {
				case "conflict":
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				case "same":
					lWalletDto = logicService.convertWalletEntityToDto(walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId())));
					break;
				case "new":
					// verify precontition
					if (Long.parseLong(lTransactionDto.getAmount())<=0
							|| lTransactionDto.getTypeTransaction()==null
							|| lTransactionDto.getTransactionId()==null
							|| ( lTransactionDto.getCashBet()==null && lTransactionDto.getTransactionId()==null )
							|| !logicService.isBetValid(lTransactionDto)) {
						return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
					}
					logicService.saveTransaction(lTransactionDto, TransactionType.BET);
					lWalletDto = logicService.createBet(lTransactionDto);
					break;
			}
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/bet : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/win")
	public ResponseEntity<WalletDto> createWin(@RequestBody TransactionDto lTransactionDto) {
		try {
			WalletDto lWalletDto = new WalletDto();
			String idempotency = logicService.verifyIdempotency(lTransactionDto);
			switch (idempotency) {
				case "conflict":
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				case "same":
					lWalletDto = logicService.convertWalletEntityToDto(walletRepository.findByPlayerId(Long.parseLong(lTransactionDto.getPlayerId())));
					break;
				case "new":
					// verify precontition
					if (Long.parseLong(lTransactionDto.getAmount())<0
							|| lTransactionDto.getTypeTransaction()==null
							|| lTransactionDto.getTransactionId()==null) {
						return new ResponseEntity<>(null, HttpStatus.PRECONDITION_FAILED);
					}
					logicService.saveTransaction(lTransactionDto, TransactionType.WIN);
					lWalletDto = logicService.createWin(lTransactionDto);
					break;
			}
			return new ResponseEntity<>(lWalletDto, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("/win : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@GetMapping("/transactions/{playerId}")
	public ResponseEntity<TransactionListDto> getTransactions(
			@PathVariable("playerId") long playerId,
			@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "100") Integer pageSize,
			@RequestParam(defaultValue = "dateTransaction") String sortBy) {
		try {
			TransactionListDto lTransactionListDto = logicService.findAllTransactions(playerId, pageNo, pageSize, sortBy);
			if (lTransactionListDto.getData().isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
			WalletEntity lWallet = new WalletEntity();
			if (playerId > 0)
				lWallet = walletRepository.findByPlayerId(playerId);
			if (lWallet == null) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(logicService.convertWalletEntityToDto(lWallet), HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("/balance : "+e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}