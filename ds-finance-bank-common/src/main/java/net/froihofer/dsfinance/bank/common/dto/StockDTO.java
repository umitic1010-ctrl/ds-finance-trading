package net.froihofer.dsfinance.bank.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Stock information
 */
public class StockDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private String currency;

    public StockDTO() {
    }

    public StockDTO(String symbol, String name, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

