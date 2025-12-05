package net.froihofer.dsfinance.bank.common.remote;

import java.util.List;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.bank.common.dto.TradeRequestDTO;
import net.froihofer.dsfinance.bank.common.exception.BankingException;

/**
 * Remote interface for customer self-service operations exposed via EJB.
 */
public interface CustomerBankingRemote {
    DepotDTO getMyDepot() throws BankingException;

    void buyStocks(TradeRequestDTO request) throws BankingException;

    void sellStocks(TradeRequestDTO request) throws BankingException;

    List<StockDTO> searchStocks(String query) throws BankingException;
}
