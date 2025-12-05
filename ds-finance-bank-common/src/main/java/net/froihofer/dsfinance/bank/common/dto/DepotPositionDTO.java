package net.froihofer.dsfinance.bank.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for a single position in a customer's depot
 */
public class DepotPositionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockSymbol;
    private String stockName;
    private Integer quantity;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;

    public DepotPositionDTO() {
    }

    public DepotPositionDTO(String stockSymbol, String stockName, Integer quantity, BigDecimal currentPrice) {
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
        this.quantity = quantity;
        this.currentPrice = currentPrice;
        this.totalValue = currentPrice.multiply(new BigDecimal(quantity));
    }

    // Getters and Setters
    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
}

