package net.froihofer.dsfinance.bank.ejb.rest;

/**
 * Generic success response for REST API
 */
public class SuccessResponse {
    private String message;
    private long timestamp;

    public SuccessResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public SuccessResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

