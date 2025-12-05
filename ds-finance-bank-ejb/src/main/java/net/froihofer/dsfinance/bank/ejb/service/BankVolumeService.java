package net.froihofer.dsfinance.bank.ejb.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.bank.ejb.entity.BankVolume;
import net.froihofer.dsfinance.bank.common.dto.BankVolumeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Bean for managing Bank's investable volume
 * Methods are @PermitAll to allow startup initialization without authentication
 */
@Stateless
@PermitAll
public class BankVolumeService {
    private static final Logger log = LoggerFactory.getLogger(BankVolumeService.class);

    @PersistenceContext(unitName = "ds-finance-bank-ref-persunit")
    private EntityManager em;

    private static final BigDecimal INITIAL_VOLUME = new BigDecimal("1000000000.00"); // 1 billion

    /**
     * Initialize bank volume if not exists
     * @return the initialized or existing bank volume
     */
    public BankVolume initializeBankVolume() {
        BankVolume volume = getCurrentBankVolume();
        if (volume == null) {
            log.info("Initializing bank volume with {} USD", INITIAL_VOLUME);
            volume = new BankVolume(INITIAL_VOLUME);
            em.persist(volume);
            em.flush(); // Force immediate persistence
            log.info("Bank volume initialized successfully");
        } else {
            log.debug("Bank volume already exists");
        }
        return volume;
    }

    /**
     * Get current bank volume
     */
    public BankVolumeDTO getBankVolume() {
        BankVolume volume = getCurrentBankVolume();
        if (volume == null) {
            log.warn("Bank volume not found, initializing...");
            volume = initializeBankVolume();
        }

        if (volume == null) {
            throw new IllegalStateException("Failed to initialize bank volume");
        }

        return toDTO(volume);
    }

    /**
     * Decrease available volume (after buying stocks)
     */
    public void decreaseVolume(BigDecimal amount) {
        log.info("Decreasing bank volume by: {}", amount);
        BankVolume volume = getCurrentBankVolume();
        if (volume == null) {
            throw new IllegalStateException("Bank volume not initialized");
        }

        volume.decreaseVolume(amount);
        em.merge(volume);
    }

    /**
     * Increase available volume (after selling stocks)
     */
    public void increaseVolume(BigDecimal amount) {
        log.info("Increasing bank volume by: {}", amount);
        BankVolume volume = getCurrentBankVolume();
        if (volume == null) {
            throw new IllegalStateException("Bank volume not initialized");
        }

        volume.increaseVolume(amount);
        em.merge(volume);
    }

    /**
     * Check if there's enough volume for a purchase
     */
    public boolean hasEnoughVolume(BigDecimal requiredAmount) {
        BankVolume volume = getCurrentBankVolume();
        if (volume == null) {
            return false;
        }

        return volume.getAvailableVolume().compareTo(requiredAmount) >= 0;
    }

    /**
     * Get current bank volume entity (internal use)
     */
    private BankVolume getCurrentBankVolume() {
        TypedQuery<BankVolume> query = em.createNamedQuery("BankVolume.findCurrent", BankVolume.class);
        List<BankVolume> volumes = query.getResultList();
        return volumes.isEmpty() ? null : volumes.get(0);
    }

    /**
     * Convert entity to DTO
     */
    private BankVolumeDTO toDTO(BankVolume volume) {
        return new BankVolumeDTO(volume.getAvailableVolume(), volume.getInitialVolume());
    }
}

