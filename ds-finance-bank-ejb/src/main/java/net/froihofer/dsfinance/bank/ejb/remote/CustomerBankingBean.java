package net.froihofer.dsfinance.bank.ejb.remote;

import java.security.Principal;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Remote;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.bank.common.dto.TradeRequestDTO;
import net.froihofer.dsfinance.bank.common.exception.BankingException;
import net.froihofer.dsfinance.bank.common.remote.CustomerBankingRemote;
import net.froihofer.dsfinance.bank.ejb.service.BankFacadeService;

/**
 * Stateless remote bean that exposes customer self-service operations.
 */
@Stateless(name = "CustomerBankingBean")
@Remote(CustomerBankingRemote.class)
@RolesAllowed("customer")
public class CustomerBankingBean implements CustomerBankingRemote {
    @EJB
    private BankFacadeService bankFacadeService;

    @Resource
    private SessionContext sessionContext;

    @Override
    public DepotDTO getMyDepot() throws BankingException {
        String customerNumber = requireAuthenticatedCustomer();
        try {
            return bankFacadeService.getDepot(customerNumber);
        } catch (IllegalArgumentException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to load depot", e);
        }
    }

    @Override
    public void buyStocks(TradeRequestDTO request) throws BankingException {
        String customerNumber = requireAuthenticatedCustomer();
        validateTradeRequest(request);
        try {
            bankFacadeService.buyStocks(customerNumber, request.getStockSymbol().trim(), request.getQuantity());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to execute buy order", e);
        }
    }

    @Override
    public void sellStocks(TradeRequestDTO request) throws BankingException {
        String customerNumber = requireAuthenticatedCustomer();
        validateTradeRequest(request);
        try {
            bankFacadeService.sellStocks(customerNumber, request.getStockSymbol().trim(), request.getQuantity());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to execute sell order", e);
        }
    }

    @Override
    public List<StockDTO> searchStocks(String query) throws BankingException {
        try {
            return bankFacadeService.searchStocks(query == null ? "" : query);
        } catch (Exception e) {
            throw new BankingException("Failed to search stocks", e);
        }
    }

    private void validateTradeRequest(TradeRequestDTO request) throws BankingException {
        if (request == null) {
            throw new BankingException("Trade request is required");
        }
        if (request.getStockSymbol() == null || request.getStockSymbol().isBlank()) {
            throw new BankingException("Stock symbol is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BankingException("Quantity must be greater than zero");
        }
    }

    private String requireAuthenticatedCustomer() throws BankingException {
        Principal principal = sessionContext.getCallerPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new BankingException("No authenticated customer context available");
        }
        return principal.getName();
    }
}
