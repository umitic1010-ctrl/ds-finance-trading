package net.froihofer.dsfinance.bank.ejb.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Stateless
@PermitAll
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_CUSTOMER_ID = "customerId";

    @Inject
    private Config config;

    public String generateToken(Long personId, String role, Long customerId, String username) throws JwtGenerationException {
        try {
            byte[] secret = getSecret();
            log.info("Generating JWT for username '{}' (secret length: {})", username, secret.length);

            Instant now = Instant.now();
            Duration ttl = Duration.ofMinutes(getTtlMinutes());
            Instant exp = now.plus(ttl);

            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(String.valueOf(personId))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim(CLAIM_ROLE, role);

            if (customerId != null) {
                builder.claim(CLAIM_CUSTOMER_ID, customerId);
            }

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (KeyLengthException e) {
            log.error("JWT secret is too weak for HS256", e);
            throw new JwtGenerationException("JWT secret is too weak", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid data for JWT generation", e);
            throw new JwtGenerationException("Invalid JWT payload", e);
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            throw new JwtGenerationException("Failed to sign JWT", e);
        }
    }

    public JwtClaims validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT is missing");
        }
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(getSecret());
            if (!jwt.verify(verifier)) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new IllegalArgumentException("JWT has expired");
            }

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("JWT subject missing");
            }

            String role = Optional.ofNullable(claims.getStringClaim(CLAIM_ROLE)).orElse("");
            Long customerId = claims.getLongClaim(CLAIM_CUSTOMER_ID);

            return new JwtClaims(Long.valueOf(subject), role, customerId, exp.toInstant());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to validate JWT", e);
            throw new IllegalArgumentException("Invalid JWT");
        }
    }

    private int getTtlMinutes() {
        return config == null ? 60 : config.getOptionalValue("auth.jwt.ttlMinutes", Integer.class).orElse(60);
    }

    private byte[] getSecret() {
        String secret = config == null ? null : config.getOptionalValue("auth.jwt.secret", String.class).orElse(null);
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public record JwtClaims(Long personId, String role, Long customerId, Instant expiresAt) {}
}
