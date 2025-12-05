package net.froihofer.dsfinance.bank.ejb.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * JPA Entity for Bank's investable volume
 */
@Entity
@Table(name = "bank_volume")
@NamedQueries({
    @NamedQuery(name = "BankVolume.findCurrent", query = "SELECT b FROM BankVolume b ORDER BY b.createdAt DESC")
})
public class BankVolume implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "available_volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableVolume;

    @Column(name = "initial_volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialVolume;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public BankVolume() {
    }

    public BankVolume(BigDecimal initialVolume) {
        this.initialVolume = initialVolume;
        this.availableVolume = initialVolume;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void decreaseVolume(BigDecimal amount) {
        this.availableVolume = this.availableVolume.subtract(amount);
    }

    public void increaseVolume(BigDecimal amount) {
        this.availableVolume = this.availableVolume.add(amount);
    }
}
