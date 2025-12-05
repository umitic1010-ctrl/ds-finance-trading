package net.froihofer.dsfinance.bank.common.remote;

import java.util.List;
import net.froihofer.dsfinance.bank.common.dto.BankVolumeDTO;
import net.froihofer.dsfinance.bank.common.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.bank.common.exception.BankingException;

/**
 * Remote interface for employee-specific banking operations exposed via EJB.
 */
public interface EmployeeBankingRemote {
    CustomerDTO createCustomer(CustomerDTO customer) throws BankingException;

    List<CustomerDTO> listCustomers() throws BankingException;

    List<CustomerDTO> searchCustomers(String query) throws BankingException;

    CustomerDTO findCustomer(String customerNumber) throws BankingException;

    DepotDTO getDepot(String customerNumber) throws BankingException;

    void buyStocks(String customerNumber, String stockSymbol, int quantity) throws BankingException;

    void sellStocks(String customerNumber, String stockSymbol, int quantity) throws BankingException;

    List<StockDTO> searchStocks(String query) throws BankingException;

    BankVolumeDTO getBankVolume() throws BankingException;
}
