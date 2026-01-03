package net.froihofer.dsfinance.bank.ejb.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.DepotPositionDTO;
import net.froihofer.dsfinance.bank.ejb.dao.DepotPositionDao;
import net.froihofer.dsfinance.bank.ejb.entity.Customer;
import net.froihofer.dsfinance.bank.ejb.entity.DepotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Bean for Depot Management
 * @PermitAll allows access from REST endpoints which have their own security
 */
@Stateless
@PermitAll
public class DepotService {
    private static final Logger log = LoggerFactory.getLogger(DepotService.class);

    @EJB
    private DepotPositionDao depotPositionDao;

    @EJB
    private TradingService tradingService;

    /**
     * Get depot for a customer with current stock prices
     */
    public DepotDTO getDepot(Customer customer) {
        log.debug("Getting depot for customer: {}", customer.getCustomerNumber());

        List<DepotPosition> positions = depotPositionDao.findByCustomerId(customer.getId());

        DepotDTO depotDTO = new DepotDTO();
        depotDTO.setCustomerId(customer.getId());
        depotDTO.setCustomerNumber(customer.getCustomerNumber());

        BigDecimal totalValue = BigDecimal.ZERO;

        for (DepotPosition position : positions) {
            if (position.getQuantity() > 0) {
                // Get current price from trading service
                BigDecimal currentPrice = tradingService.getStockPrice(position.getStockSymbol());

                DepotPositionDTO positionDTO = new DepotPositionDTO(
                    position.getStockSymbol(),
                    position.getStockName(),
                    position.getQuantity(),
                    currentPrice
                );

                depotDTO.getPositions().add(positionDTO);
                totalValue = totalValue.add(positionDTO.getTotalValue());
            }
        }

        depotDTO.setTotalValue(totalValue);

        return depotDTO;
    }

    /**
     * Add stocks to depot (after buying)
     */
    public void addStocks(Customer customer, String stockSymbol, String stockName, Integer quantity) {
        log.info("Adding {} shares of {} to customer {}", quantity, stockSymbol, customer.getCustomerNumber());

        DepotPosition position = findPosition(customer, stockSymbol);

        if (position == null) {
            // Create new position
            position = new DepotPosition(customer, stockSymbol, stockName, quantity);
            depotPositionDao.persist(position);
        } else {
            // Update existing position
            position.addQuantity(quantity);
            depotPositionDao.merge(position);
        }
    }

    /**
     * Remove stocks from depot (after selling)
     */
    public boolean removeStocks(Customer customer, String stockSymbol, Integer quantity) {
        log.info("Removing {} shares of {} from customer {}", quantity, stockSymbol, customer.getCustomerNumber());

        DepotPosition position = findPosition(customer, stockSymbol);

        if (position == null || position.getQuantity() < quantity) {
            log.warn("Insufficient stocks to sell. Available: {}, Requested: {}",
                    position != null ? position.getQuantity() : 0, quantity);
            return false;
        }

        position.subtractQuantity(quantity);

        if (position.getQuantity() == 0) {
            depotPositionDao.remove(position);
        } else {
            depotPositionDao.merge(position);
        }

        return true;
    }

    /**
     * Check if customer has enough stocks to sell
     */
    public boolean hasEnoughStocks(Customer customer, String stockSymbol, Integer quantity) {
        DepotPosition position = findPosition(customer, stockSymbol);
        return position != null && position.getQuantity() >= quantity;
    }

    /**
     * Find depot position for customer and stock (internal use)
     */
    private DepotPosition findPosition(Customer customer, String stockSymbol) {
        return depotPositionDao.findByCustomerAndStock(customer.getId(), stockSymbol);
    }
}

