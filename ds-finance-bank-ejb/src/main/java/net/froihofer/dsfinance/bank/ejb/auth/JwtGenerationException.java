package net.froihofer.dsfinance.bank.ejb.auth;

public class JwtGenerationException extends Exception {
    private static final long serialVersionUID = 1L;

    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
