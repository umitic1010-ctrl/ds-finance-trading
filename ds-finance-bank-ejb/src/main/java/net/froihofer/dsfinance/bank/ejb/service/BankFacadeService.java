package net.froihofer.dsfinance.bank.ejb.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.ejb.entity.Customer;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Facade Service for Bank Operations
 * Orchestrates calls between different services
 * @PermitAll allows access from REST endpoints which have their own security
 */
@Stateless
@PermitAll
public class BankFacadeService {
    private static final Logger log = LoggerFactory.getLogger(BankFacadeService.class);

    @EJB
    private CustomerService customerService;

    @EJB
    private DepotService depotService;

    @EJB
    private TradingService tradingService;

    @EJB
    private BankVolumeService bankVolumeService;

    /**
     * Search for stocks
     */
    public List<StockDTO> searchStocks(String searchTerm) {
        return tradingService.searchStocks(searchTerm);
    }

    /**
     * Get depot for a customer
     */
    public DepotDTO getDepot(String customerNumber) {
        Customer customer = customerService.getCustomerEntityByNumber(customerNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerNumber);
        }

        return depotService.getDepot(customer);
    }

    /**
     * Buy stocks for a customer
     */
    public void buyStocks(String customerNumber, String stockSymbol, Integer quantity) {
        log.info("Processing buy order: {} shares of {} for customer {}", quantity, stockSymbol, customerNumber);

        if (customerNumber == null || customerNumber.isBlank()) {
            throw new IllegalArgumentException("Customer number must not be empty");
        }
        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol must not be empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Validate customer
        Customer customer = customerService.getCustomerEntityByNumber(customerNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerNumber);
        }

        // Get stock details
        StockDTO stock = tradingService.getStockDetails(stockSymbol);

        // Calculate total cost
        BigDecimal totalCost = tradingService.buyStocks(stockSymbol, quantity);

        // Check if bank has enough volume
        if (!bankVolumeService.hasEnoughVolume(totalCost)) {
            throw new IllegalStateException("Insufficient bank volume for this transaction");
        }

        // Execute trade
        try {
            // Decrease bank volume
            bankVolumeService.decreaseVolume(totalCost);

            // Add stocks to customer depot
            depotService.addStocks(customer, stockSymbol, stock.getName(), quantity);

            log.info("Successfully bought {} shares of {} for customer {}", quantity, stockSymbol, customerNumber);
        } catch (Exception e) {
            log.error("Error during buy transaction", e);
            // Rollback is automatic due to @Stateless EJB transaction management
            throw new RuntimeException("Failed to execute buy order: " + e.getMessage(), e);
        }
    }

    /**
     * Sell stocks for a customer
     */
    public void sellStocks(String customerNumber, String stockSymbol, Integer quantity) {
        log.info("Processing sell order: {} shares of {} for customer {}", quantity, stockSymbol, customerNumber);

        if (customerNumber == null || customerNumber.isBlank()) {
            throw new IllegalArgumentException("Customer number must not be empty");
        }
        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol must not be empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Validate customer
        Customer customer = customerService.getCustomerEntityByNumber(customerNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerNumber);
        }

        // Check if customer has enough stocks
        if (!depotService.hasEnoughStocks(customer, stockSymbol, quantity)) {
            throw new IllegalStateException("Customer does not have enough stocks to sell");
        }

        // Calculate total proceeds
        BigDecimal totalProceeds = tradingService.sellStocks(stockSymbol, quantity);

        // Execute trade
        try {
            // Remove stocks from customer depot
            depotService.removeStocks(customer, stockSymbol, quantity);

            // Increase bank volume
            bankVolumeService.increaseVolume(totalProceeds);

            log.info("Successfully sold {} shares of {} for customer {}", quantity, stockSymbol, customerNumber);
        } catch (Exception e) {
            log.error("Error during sell transaction", e);
            throw new RuntimeException("Failed to execute sell order: " + e.getMessage(), e);
        }
    }
}

