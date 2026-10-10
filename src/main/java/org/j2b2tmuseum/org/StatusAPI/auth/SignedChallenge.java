package org.j2b2tmuseum.org.StatusAPI.auth;

public record SignedChallenge(long id, byte[] challenge, String signature) {
}
