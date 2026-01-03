package net.froihofer.dsfinance.bank.ejb.auth;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Stateless
@PermitAll
public class PasswordHasher {
    private static final Logger log = LoggerFactory.getLogger(PasswordHasher.class);
    private static final int DEFAULT_ITERATIONS = 120000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;

    public PasswordHash hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return hashPassword(password, salt, DEFAULT_ITERATIONS);
    }

    public PasswordHash hashPassword(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return new PasswordHash(
                Base64.getEncoder().encodeToString(hash),
                Base64.getEncoder().encodeToString(salt),
                iterations
            );
        } catch (Exception e) {
            log.error("Failed to hash password", e);
            throw new IllegalStateException("Failed to hash password", e);
        }
    }

    public boolean verifyPassword(String password, String expectedHash, String saltBase64, int iterations) {
        if (password == null || expectedHash == null || saltBase64 == null) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        PasswordHash computed = hashPassword(password, salt, iterations);
        return expectedHash.equals(computed.hash());
    }

    public record PasswordHash(String hash, String salt, int iterations) {}
}
