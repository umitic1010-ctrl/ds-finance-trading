package net.froihofer.dsfinance.bank.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Customer Depot (Portfolio)
 */
public class DepotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String customerNumber;
    private List<DepotPositionDTO> positions;
    private BigDecimal totalValue;

    public DepotDTO() {
        this.positions = new ArrayList<>();
        this.totalValue = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public List<DepotPositionDTO> getPositions() {
        return positions;
    }

    public void setPositions(List<DepotPositionDTO> positions) {
        this.positions = positions;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
}

