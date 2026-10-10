package org.j2b2tmuseum.org.StatusAPI;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.j2b2tmuseum.org.StatusAPI.auth.ChallengeManager;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@Component
public class ChallengeComponent {

    private ChallengeManager challengeManager;

    public ChallengeComponent() {
    }

    @PostConstruct
    public void start() {
        PublicKey key = null;
        try {
            if (needsNewKeypairs()) {
                key = setUpNewKeypairs();
                System.out.println("A new private and public keypair were generated because no public key existed.");
            } else {
                key = setUpExistingKeypair();
            }
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        challengeManager = new ChallengeManager(key);
        StatusApiApplication.keyPairReadiness = true;
        System.out.println("Instantiated ChallengeManager");
    }

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    @Nullable
    private PublicKey setUpExistingKeypair() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File dir = Paths.get("keypairs").toFile();
        if (!dir.exists() || !dir.isDirectory()) return null;
        Path pub = Paths.get("keypairs/pub.key");
        byte[] b = Files.readAllBytes(pub);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec pubic = new X509EncodedKeySpec(b);
        return keyFactory.generatePublic(pubic);
    }

    private boolean needsNewKeypairs() {
        File dir = Paths.get("keypairs").toFile();
        if (!dir.exists() || !dir.isDirectory()) return true;
        File pub = Paths.get("keypairs/pub.key").toFile();
        boolean needs = false;
        if (!pub.exists()) needs = true;
        if (pub.isDirectory()) {
            pub.delete();
            needs = true;
        }
        return needs;
    }

    private PublicKey setUpNewKeypairs() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();
        File dir = Paths.get("keypairs").toFile();
        if (!dir.exists() || !dir.isDirectory()) dir.mkdir();
        Path pub = Paths.get("keypairs/pub.key");
        Path pri = Paths.get("keypairs/pri.key");
        Files.write(pub, publicKey.getEncoded());
        Files.write(pri, privateKey.getEncoded());
        return publicKey;
    }
}
