package org.j2b2tmuseum.org.StatusAPI.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Nullable;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

public class ChallengeManager {

    private final PublicKey key;

    private final Cache<Long, byte[]> challengeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(1000)
            .build();

    public ChallengeManager(PublicKey key) {
        if (key == null) throw new IllegalArgumentException("Argument key can't be null");
        this.key = key;
    }

    @Nullable
    public byte[] retrieveChallenge(long id) {
        return challengeCache.getIfPresent(id);
    }

    public byte[] issueChallenge(long id) {
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        challengeCache.put(id, challenge);
        return challenge;
    }

    public boolean verifyChallenge(Challenge challenge, String signatureB64) {
        try {
            return verifySignature(challenge.challenge(), signatureB64, getPublicKey());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyChallenge(long id, String signatureB64) {
        try {
            byte[] chal = retrieveChallenge(id);
            if (chal == null) return false;
            return verifySignature(chal, signatureB64, getPublicKey());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyChallenge(long id, SignedChallenge signedChallenge) {
        try {
            byte[] chal = retrieveChallenge(id);
            if (chal == null) return false;
            if (!Arrays.equals(signedChallenge.challenge(), chal)) {
                System.err.println("Client returned a challenge other than what we have cached, tampering may be evident.");
                return false;
            }
            return verifySignature(chal, signedChallenge.signature(), getPublicKey());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyChallenge(SignedChallenge signedChallenge) {
        try {
            long id = signedChallenge.id();
            byte[] chal = retrieveChallenge(id);
            if (chal == null) return false;
            if (!Arrays.equals(signedChallenge.challenge(), chal)) {
                System.err.println("Client returned a challenge other than what we have cached, tampering may be evident.");
                return false;
            }
            return verifySignature(chal, signedChallenge.signature(), getPublicKey());
        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey getPublicKey() {
        return key;
    }

    private static final String ALGORITHM = "SHA256withRSA";

    // sign with privkey
    private static String signChallenge(byte[] challengeBytes, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(privateKey);
        signer.update(challengeBytes);
        byte[] signatureBytes = signer.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // verify sig with pubkey
    private static boolean verifySignature(byte[] challengeBytes, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance(ALGORITHM);
        verifier.initVerify(publicKey);
        verifier.update(challengeBytes);
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return verifier.verify(signatureBytes);
    }
}
