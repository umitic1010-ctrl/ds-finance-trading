package net.froihofer.dsfinance.bank.common.dto;

import java.io.Serializable;

/**
 * Data Transfer Object for Buy/Sell trade requests
 */
public class TradeRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockSymbol;
    private Integer quantity;
    private String customerNumber; // Only for employee operations

    public TradeRequestDTO() {
    }

    public TradeRequestDTO(String stockSymbol, Integer quantity) {
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }
}

