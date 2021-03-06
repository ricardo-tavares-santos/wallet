package com.ricardo.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ricardo.demo.dto.TransactionDto;
import com.ricardo.demo.dto.TransactionSaveDto;
import com.ricardo.demo.model.PlayerEntity;
import com.ricardo.demo.repository.PlayerRepository;
import com.ricardo.demo.service.idempotency.TokenService;
import com.ricardo.demo.service.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RicardoDemoApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	PlayerRepository userRepository;

	@Test
	public void should_AllowAccess() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/public")).andExpect(status().isOk());
	}

	@Test
	public void should_NotAllowAccess() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/secret")).andExpect(status().isForbidden());
	}

	@Test
	public void should_NotAllowAccessToUnauthenticatedUsers() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/transactions/0")).andExpect(status().isForbidden());
	}

	@Test
	public void should_InsertUser_GenerateAuthToken_DeleteUser() throws Exception {
		String email = "application@test.com";
		PlayerEntity user = new PlayerEntity("name", false, email, "password");
		userRepository.save(user);
		List<PlayerEntity> users = userRepository.findByEmail(email);
		assertNotNull(users.size()>0?true:null);

		String token = this.tokenProvider.createToken(user);
		assertNotNull(token);
		mvc.perform(MockMvcRequestBuilders.get("/secret").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

		userRepository.deleteById(users.stream().findFirst().get().getId());
		users = userRepository.findByEmail(email);

		assertNull(users.size()>0?true:null);
	}

	@Autowired
	private TokenService tokenService;

	@Test
	public void should_Itempotency() throws Exception {
		String token = tokenService.createToken();
		assertNotNull(token);
		TransactionSaveDto lTransactionSaveDto = new TransactionSaveDto();
		lTransactionSaveDto.setIdempotency_Key(token);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
				.post("/idempotency")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson)).andExpect(status().isOk());
		mvc.perform(MockMvcRequestBuilders
				.post("/idempotency")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson)).andExpect(status().isConflict());
	}

	@Test
	public void should_Not_Itempotency() throws Exception {
		String token = "7777777777777777777777777 because i can ";
		TransactionSaveDto lTransactionSaveDto = new TransactionSaveDto();
		lTransactionSaveDto.setIdempotency_Key(token);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson=ow.writeValueAsString(lTransactionSaveDto);
		mvc.perform(MockMvcRequestBuilders
				.post("/idempotency")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson)).andExpect(status().isConflict());
	}

}
