package net.froihofer.dsfinance.bank.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Bank's investable volume
 */
public class BankVolumeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal availableVolume;
    private BigDecimal initialVolume;
    private String currency;

    public BankVolumeDTO() {
    }

    public BankVolumeDTO(BigDecimal availableVolume, BigDecimal initialVolume) {
        this.availableVolume = availableVolume;
        this.initialVolume = initialVolume;
        this.currency = "USD";
    }

    // Getters and Setters
    public BigDecimal getAvailableVolume() {
        return availableVolume;
    }

    public void setAvailableVolume(BigDecimal availableVolume) {
        this.availableVolume = availableVolume;
    }

    public BigDecimal getInitialVolume() {
        return initialVolume;
    }

    public void setInitialVolume(BigDecimal initialVolume) {
        this.initialVolume = initialVolume;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

