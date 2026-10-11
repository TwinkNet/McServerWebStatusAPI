package org.j2b2tmuseum.org.StatusAPI;

import jakarta.annotation.Nullable;
import org.j2b2tmuseum.org.StatusAPI.auth.Challenge;
import org.j2b2tmuseum.org.StatusAPI.auth.SignedChallenge;
import org.j2b2tmuseum.org.StatusAPI.obj.Result;
import org.j2b2tmuseum.org.StatusAPI.obj.ServerStat;
import org.j2b2tmuseum.org.StatusAPI.obj.State;
import org.j2b2tmuseum.org.StatusAPI.updatestats.PostUpdateRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class StatusApiApplicationTests {

	@Deprecated private static final String SERVER_STAT = "{\"uniquePlayers\":4783,\"currentPlayers\":20,\"worldSizeBytes\":10034}";
	private static final ServerStat OBJ_SERVER_STAT = new ServerStat(4783, 20, 10034);

	@Autowired
	MockMvc mvc;
	@Autowired
	ObjectMapper mapper;

	private static SignedChallenge signedChallenge;

	@Test
	@Order(value = 1) // signed challenge needs generated first before other tests can run
	void getChallengeController() throws Exception {
		mvc.perform(
				get("/challenge/1")
		).andExpect(
				result -> {
					String response = result.getResponse().getContentAsString();
					Challenge res = mapper.readValue(response, Challenge.class);
					// got challenge
					PrivateKey key = Util.setUpExistingKeypair();
					String sig = Util.signChallenge(res.challenge(), key);
					// generated signature
					signedChallenge = new SignedChallenge(1L, res.challenge(), sig);
					String json = mapper.writeValueAsString(signedChallenge);
					// created SignedChallenge and mapped to json
					System.out.println(json);
					mvc.perform(post("/challenge/1")
							.contentType(MediaType.APPLICATION_JSON)
							.content(json))
							.andExpect(pResult -> {
								String pResponse = pResult.getResponse().getContentAsString();
								Result pRes = mapper.readValue(pResponse, Result.class);
								assertTrue(pRes.getState() == State.OK);
								System.out.println("postChallengeController -- OK, AUTHENTICATED");
							});
					assertTrue(res.challenge().length == 32);
					System.out.println("getChallengeController -- OK");
				}
		);
	}

	@Test
	void postUpdateStatsController() throws Exception {
		ServerStat stat = OBJ_SERVER_STAT;
		PostUpdateRequest request = new PostUpdateRequest(signedChallenge, stat);
		String json = mapper.writeValueAsString(request);
		System.out.println(json);
		mvc.perform(
				post("/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andExpect(
				result -> {
					String response = result.getResponse().getContentAsString();
					System.out.println(response);
					Result res = mapper.readValue(response, Result.class);
					assertTrue(res.getState() == State.OK);
					assertTrue(StatusApiApplication.globalServerStat.equals(stat));
					System.out.println("postUpdateStatsController -- OK");
				}
		);
	}

	@Test
	void patchUpdateStatsControllerUniquePlayers() throws Exception {
		final int newUniquePlayers = 4883;
		String json = mapper.writeValueAsString(signedChallenge);
		System.out.println(json);
		mvc.perform(
				patch("/update?uniquePlayers=" + newUniquePlayers)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andExpect(
				result -> {
					String response = result.getResponse().getContentAsString();
					System.out.println(response);
					Result res = mapper.readValue(response, Result.class);
					assertTrue(res.getState() == State.OK);
					boolean updated = StatusApiApplication.globalServerStat.uniquePlayers() == newUniquePlayers;
					assertTrue(updated);
					System.out.println("patchUpdateStatsControllerUniquePlayers -- OK");

				}
		);
	}

	@Test
	void patchUpdateStatsControllerUniquePlayersValidation() throws Exception {
		final int newUniquePlayers = -4883;
		final int currentUniquePlayers = StatusApiApplication.globalServerStat.uniquePlayers();
		String json = mapper.writeValueAsString(signedChallenge);
		System.out.println(json);
		mvc.perform(
				patch("/update?uniquePlayers=" + newUniquePlayers)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andExpect(
				result -> {
					String response = result.getResponse().getContentAsString();
					System.out.println(response);
					Result res = mapper.readValue(response, Result.class);
					assertTrue(res.getState() == State.ERROR);
					boolean didntUpdate = StatusApiApplication.globalServerStat.uniquePlayers() == currentUniquePlayers;
					assertTrue(didntUpdate);
					System.out.println("patchUpdateStatsControllerUniquePlayersValidation -- OK");

				}
		);
	}

	@Test
	void patchUpdateStatsControllerCurrentPlayers() throws Exception {
		final int newCurrentPlayers = 24;
		String json = mapper.writeValueAsString(signedChallenge);
		System.out.println(json);
		mvc.perform(
				patch("/update?currentPlayers=" + newCurrentPlayers)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andExpect(
				result -> {
					String response = result.getResponse().getContentAsString();
					System.out.println(response);
					Result res = mapper.readValue(response, Result.class);
					assertTrue(res.getState() == State.OK);
					boolean updated = StatusApiApplication.globalServerStat.currentPlayers() == newCurrentPlayers;
					assertTrue(updated);
					System.out.println("patchUpdateStatsControllerCurrentPlayers -- OK");

				}
		);
	}
	@Test
	void patchUpdateStatsControllerWorldSizeBytes() throws Exception {
		final long newWorldSizeBytes = 1003463;
		String json = mapper.writeValueAsString(signedChallenge);
		System.out.println(json);
		mvc.perform(
				patch("/update?worldSizeBytes=" + newWorldSizeBytes)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andExpect(
				result -> {
					String response = result.getResponse().getContentAsString();
					Result res = mapper.readValue(response, Result.class);
					assertTrue(res.getState() == State.OK);
					boolean updated = StatusApiApplication.globalServerStat.worldSizeBytes() == newWorldSizeBytes;
					assertTrue(updated);
					System.out.println("patchUpdateStatsControllerWorldSizeBytes -- OK");

				}
		);
	}

	private void assertTrue(boolean flag) {
		assertThat(flag).isTrue();
	}

}
class Util {
	@Nullable
	/* pkg priv */ static PrivateKey setUpExistingKeypair() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		File dir = Paths.get("keypairs").toFile();
		if (!dir.exists() || !dir.isDirectory()) return null;
		Path pub = Paths.get("keypairs/pri.key");
		if (!pub.toFile().exists()) {
			throw new RuntimeException("[TESTS] The tests can't complete without the pri.key in the keypairs directory");
		}
		byte[] b = Files.readAllBytes(pub);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec pubic = new PKCS8EncodedKeySpec (b);
		return keyFactory.generatePrivate(pubic);
	}

	/* pkg priv */ static String signChallenge(byte[] challengeBytes, PrivateKey privateKey) throws Exception {
		Signature signer = Signature.getInstance("SHA256withRSA");
		signer.initSign(privateKey);
		signer.update(challengeBytes);
		byte[] signatureBytes = signer.sign();
		return Base64.getEncoder().encodeToString(signatureBytes);
	}
}