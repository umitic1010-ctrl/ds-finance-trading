package net.froihofer.dsfinance.bank.ejb.remote;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.common.dto.BankVolumeDTO;
import net.froihofer.dsfinance.bank.common.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.bank.common.exception.BankingException;
import net.froihofer.dsfinance.bank.common.remote.EmployeeBankingRemote;
import net.froihofer.dsfinance.bank.ejb.service.BankFacadeService;
import net.froihofer.dsfinance.bank.ejb.service.BankVolumeService;
import net.froihofer.dsfinance.bank.ejb.service.CustomerService;

/**
 * Stateless remote bean that exposes all employee-facing banking operations.
 */
@Stateless(name = "EmployeeBankingBean")
@Remote(EmployeeBankingRemote.class)
@RolesAllowed("employee")
public class EmployeeBankingBean implements EmployeeBankingRemote {
    @EJB
    private CustomerService customerService;

    @EJB
    private BankFacadeService bankFacadeService;

    @EJB
    private BankVolumeService bankVolumeService;

    @Override
    public CustomerDTO createCustomer(CustomerDTO customer) throws BankingException {
        requireCustomerDetails(customer);
        try {
            return customerService.createCustomer(customer);
        } catch (Exception e) {
            throw new BankingException("Failed to create customer", e);
        }
    }

    @Override
    public List<CustomerDTO> listCustomers() throws BankingException {
        try {
            return customerService.getAllCustomers();
        } catch (Exception e) {
            throw new BankingException("Failed to fetch customers", e);
        }
    }

    @Override
    public List<CustomerDTO> searchCustomers(String query) throws BankingException {
        try {
            if (query == null || query.isBlank()) {
                return customerService.getAllCustomers();
            }

            Map<String, CustomerDTO> unique = new LinkedHashMap<>();

            CustomerDTO byNumber = customerService.findByCustomerNumber(query.trim());
            if (byNumber != null) {
                unique.put(byNumber.getCustomerNumber(), byNumber);
            }

            customerService.searchByName(query.trim()).forEach(dto -> {
                if (dto.getCustomerNumber() != null) {
                    unique.putIfAbsent(dto.getCustomerNumber(), dto);
                }
            });

            List<CustomerDTO> result = new ArrayList<>(unique.values());

            if (result.isEmpty()) {
                throw new BankingException("No customer matches query: " + query);
            }

            return result;
        } catch (BankingException e) {
            throw e;
        } catch (Exception e) {
            throw new BankingException("Failed to search customers", e);
        }
    }

    public void deleteCustomer(String customerNumber) throws BankingException {
        if (customerNumber == null || customerNumber.isBlank()) {
            throw new BankingException("Customer number must not be empty");
        }
        try {
            customerService.deleteCustomer(customerNumber.trim());
        } catch (IllegalArgumentException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to delete customer", e);
        }
    }

    public void updateCustomer(CustomerDTO customer) throws BankingException {
        requireCustomerDetails(customer);
        if (customer.getCustomerNumber() == null || customer.getCustomerNumber().isBlank()) {
            throw new BankingException("Customer number must not be empty");
        }
        try {
            customerService.updateCustomer(customer);
        } catch (IllegalArgumentException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to update customer", e);
        }
    }

    @Override
    public CustomerDTO findCustomer(String customerNumber) throws BankingException {
        if (customerNumber == null || customerNumber.isBlank()) {
            throw new BankingException("Customer number must not be empty");
        }
        try {
            CustomerDTO dto = customerService.findByCustomerNumber(customerNumber.trim());
            if (dto == null) {
                throw new BankingException("No customer found for number " + customerNumber);
            }
            return dto;
        } catch (BankingException e) {
            throw e;
        } catch (Exception e) {
            throw new BankingException("Failed to find customer", e);
        }
    }

    @Override
    public DepotDTO getDepot(String customerNumber) throws BankingException {
        if (customerNumber == null || customerNumber.isBlank()) {
            throw new BankingException("Customer number must not be empty");
        }
        try {
            return bankFacadeService.getDepot(customerNumber.trim());
        } catch (IllegalArgumentException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to load depot", e);
        }
    }

    @Override
    public void buyStocks(String customerNumber, String stockSymbol, int quantity) throws BankingException {
        validateTradeInput(customerNumber, stockSymbol, quantity);
        try {
            bankFacadeService.buyStocks(customerNumber.trim(), stockSymbol.trim(), quantity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BankingException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankingException("Failed to execute buy order", e);
        }
    }

    @Override
    public void sellStocks(String customerNumber, String stockSymbol, int quantity) throws BankingException {
        validateTradeInput(customerNumber, stockSymbol, quantity);
        try {
            bankFacadeService.sellStocks(customerNumber.trim(), stockSymbol.trim(), quantity);
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

    @Override
    public BankVolumeDTO getBankVolume() throws BankingException {
        try {
            return bankVolumeService.getBankVolume();
        } catch (Exception e) {
            throw new BankingException("Failed to load bank volume", e);
        }
    }

    private void requireCustomerDetails(CustomerDTO customer) throws BankingException {
        if (customer == null) {
            throw new BankingException("Customer payload is required");
        }
        if (isNullOrBlank(customer.getFirstName()) || isNullOrBlank(customer.getLastName())) {
            throw new BankingException("Customer first and last name are required");
        }
    }

    private void validateTradeInput(String customerNumber, String stockSymbol, int quantity) throws BankingException {
        if (isNullOrBlank(customerNumber)) {
            throw new BankingException("Customer number is required");
        }
        if (isNullOrBlank(stockSymbol)) {
            throw new BankingException("Stock symbol is required");
        }
        if (quantity <= 0) {
            throw new BankingException("Quantity must be greater than zero");
        }
    }

    private boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }
}
