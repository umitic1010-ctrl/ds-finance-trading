package net.froihofer.dsfinance.bank.common.exception;

/**
 * Checked exception that represents business errors returned by the bank server
 * when invoking remote EJB interfaces from clients.
 */
public class BankingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
