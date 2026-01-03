package net.froihofer.dsfinance.bank.ejb.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceRef;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import jakarta.xml.ws.http.HTTPException;
import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Bean that integrates the external Trading SOAP Web Service.
 * All methods delegate to {@code https://edu.dedisys.org/ds-finance/ws/TradingService}
 * using the generated JAX-WS client stubs. Optional authentication and timeouts
 * are configurable via MicroProfile Config (see microprofile-config.properties).
 *
 * @PermitAll allows access from REST endpoints which apply their own security
 * constraints.
 */
@Stateless
@PermitAll
public class TradingService {
    private static final Logger log = LoggerFactory.getLogger(TradingService.class);

    private static final String DEFAULT_CURRENCY = "USD";
    private static final String CONNECT_TIMEOUT_PROPERTY = "com.sun.xml.ws.connect.timeout";
    private static final String REQUEST_TIMEOUT_PROPERTY = "com.sun.xml.ws.request.timeout";
    private static final List<String> FALLBACK_SYMBOLS = List.of("AAPL", "MSFT", "GOOGL", "AMZN");

    @WebServiceRef(TradingWebServiceService.class)
    private TradingWebServiceService tradingWebServiceService;

    @Inject
    private Config config;

    /**
     * Search for stocks via SOAP TradingService.
     */
    public List<StockDTO> searchStocks(String searchTerm) {
        String needle = searchTerm == null ? "" : searchTerm.trim();
        log.info("Searching stocks with term: '{}'", needle);

        try {
            TradingWebService port = getPort();
            List<PublicStockQuote> quotes;

            if (needle.isEmpty()) {
                quotes = port.getStockQuotes(getDefaultSymbols());
            } else {
                quotes = port.findStockQuotesByCompanyName(needle);
            }

            if (quotes == null || quotes.isEmpty()) {
                return Collections.emptyList();
            }

            return quotes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        } catch (TradingWSException_Exception e) {
            log.warn("Trading service rejected search '{}': {}", needle, faultMessage(e));
            return Collections.emptyList();
        } catch (WebServiceException e) {
            if (isUnauthorized(e)) {
                log.error("Trading service authentication failed for search '{}'. Check trading.ws.username/password configuration.", needle);
                return fallbackQuotes(needle);
            }
            throw new RuntimeException("Failed to query trading service", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query trading service", e);
        }
    }

    /**
     * Returns the latest price for the given stock symbol.
     */
    public BigDecimal getStockPrice(String stockSymbol) {
        return getStockDetails(stockSymbol).getCurrentPrice();
    }

    /**
     * Executes a buy order and returns the total cost (price per share * quantity).
     */
    public BigDecimal buyStocks(String stockSymbol, Integer quantity) {
        validateSymbol(stockSymbol);
        validateQuantity(quantity);

        log.info("Buying {} shares of {} via trading service", quantity, stockSymbol);

        try {
            TradingWebService port = getPort();
            BigDecimal pricePerShare = port.buy(stockSymbol.trim(), quantity);
            if (pricePerShare == null) {
                throw new IllegalStateException("Trading service returned no price for buy order of " + stockSymbol);
            }
            return pricePerShare.multiply(BigDecimal.valueOf(quantity));
        } catch (TradingWSException_Exception e) {
            throw new IllegalStateException("Trading service rejected buy order: " + faultMessage(e), e);
        } catch (WebServiceException e) {
            throw translateWebServiceException("executing buy order for " + stockSymbol, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute buy order with trading service", e);
        }
    }

    /**
     * Executes a sell order and returns the total proceeds (price per share * quantity).
     */
    public BigDecimal sellStocks(String stockSymbol, Integer quantity) {
        validateSymbol(stockSymbol);
        validateQuantity(quantity);

        log.info("Selling {} shares of {} via trading service", quantity, stockSymbol);

        try {
            TradingWebService port = getPort();
            BigDecimal pricePerShare = port.sell(stockSymbol.trim(), quantity);
            if (pricePerShare == null) {
                throw new IllegalStateException("Trading service returned no price for sell order of " + stockSymbol);
            }
            return pricePerShare.multiply(BigDecimal.valueOf(quantity));
        } catch (TradingWSException_Exception e) {
            throw new IllegalStateException("Trading service rejected sell order: " + faultMessage(e), e);
        } catch (WebServiceException e) {
            throw translateWebServiceException("executing sell order for " + stockSymbol, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute sell order with trading service", e);
        }
    }

    /**
     * Fetches detailed quote information for the given symbol.
     */
    public StockDTO getStockDetails(String stockSymbol) {
        validateSymbol(stockSymbol);

        String trimmed = stockSymbol.trim();
        log.debug("Fetching stock details for {}", trimmed);

        try {
            TradingWebService port = getPort();
            List<PublicStockQuote> quotes = port.getStockQuotes(Collections.singletonList(trimmed));

            if (quotes == null || quotes.isEmpty()) {
                throw new IllegalArgumentException("No quote found for symbol " + trimmed);
            }

            return toDto(quotes.get(0));
        } catch (TradingWSException_Exception e) {
            throw new IllegalStateException("Trading service rejected symbol " + stockSymbol + ": " + faultMessage(e), e);
        } catch (WebServiceException e) {
            throw translateWebServiceException("loading stock details for " + stockSymbol, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load stock details for " + stockSymbol, e);
        }
    }

    private TradingWebService getPort() {
        try {
            TradingWebServiceService service = tradingWebServiceService != null ? tradingWebServiceService : new TradingWebServiceService();
            TradingWebService port = service.getTradingWebServicePort();
            configurePort((BindingProvider) port);
            return port;
        } catch (WebServiceException e) {
            throw new RuntimeException("Unable to initialise trading web service client", e);
        }
    }

    private void configurePort(BindingProvider bindingProvider) {
        getConfigValue("trading.ws.endpoint")
            .filter(url -> !url.isBlank())
            .ifPresent(url -> bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url));

        getConfigValue("trading.ws.username")
            .filter(username -> !username.isBlank())
            .ifPresent(username -> bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username));

        getConfigValue("trading.ws.password")
            .filter(password -> !password.isBlank())
            .ifPresent(password -> bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password));

        getIntConfig("trading.ws.connectTimeout")
            .ifPresent(timeout -> bindingProvider.getRequestContext().put(CONNECT_TIMEOUT_PROPERTY, timeout));

        getIntConfig("trading.ws.requestTimeout")
            .ifPresent(timeout -> bindingProvider.getRequestContext().put(REQUEST_TIMEOUT_PROPERTY, timeout));
    }

    private Optional<String> getConfigValue(String key) {
        return config == null ? Optional.empty() : config.getOptionalValue(key, String.class);
    }

    private Optional<Integer> getIntConfig(String key) {
        return config == null ? Optional.empty() : config.getOptionalValue(key, Integer.class);
    }

    private List<String> getDefaultSymbols() {
        return getConfigValue("trading.ws.defaultSymbols")
            .map(value -> Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList()))
            .filter(list -> !list.isEmpty())
            .orElse(FALLBACK_SYMBOLS);
    }

    private List<StockDTO> fallbackQuotes(String needle) {
        String lowered = needle == null ? "" : needle.toLowerCase();
        List<StockDTO> fallback = getDefaultSymbols().stream()
            .filter(symbol -> lowered.isBlank() || symbol.toLowerCase().contains(lowered))
            .map(symbol -> {
                StockDTO dto = new StockDTO();
                dto.setSymbol(symbol);
                dto.setName(symbol);
                dto.setCurrentPrice(BigDecimal.ZERO);
                dto.setCurrency(DEFAULT_CURRENCY);
                return dto;
            })
            .collect(Collectors.toList());

        log.warn("Returning {} fallback quote(s) for search '{}' due to trading service authentication failure", fallback.size(), needle);
        return fallback;
    }

    private StockDTO toDto(PublicStockQuote quote) {
        if (quote == null) {
            return new StockDTO();
        }

        StockDTO dto = new StockDTO();
        dto.setSymbol(quote.getSymbol());
        dto.setName(Optional.ofNullable(quote.getCompanyName()).orElse(quote.getSymbol()));
        dto.setCurrentPrice(Optional.ofNullable(quote.getLastTradePrice()).orElse(BigDecimal.ZERO));
        dto.setCurrency(DEFAULT_CURRENCY);
        return dto;
    }

    private void validateSymbol(String stockSymbol) {
        if (stockSymbol == null || stockSymbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol must not be empty");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private String faultMessage(TradingWSException_Exception e) {
        if (e == null) {
            return "n/a";
        }
        return Optional.ofNullable(e.getFaultInfo())
            .map(info -> Optional.ofNullable(info.getMessage()).orElse("n/a"))
            .orElseGet(() -> safeMessage(e));
    }

    private String safeMessage(Throwable t) {
        return Optional.ofNullable(t)
            .map(Throwable::getMessage)
            .filter(message -> !message.isBlank())
            .orElse("n/a");
    }

    private RuntimeException translateWebServiceException(String action, WebServiceException e) {
        if (isUnauthorized(e)) {
            return new IllegalStateException("Trading service authentication failed while " + action + ". Configure trading.ws.username/password.", e);
        }
        return new RuntimeException("Trading service communication failed while " + action, e);
    }

    private boolean isUnauthorized(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (isHttpUnauthorized(cursor)) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private boolean isHttpUnauthorized(Throwable throwable) {
        if (throwable instanceof HTTPException) {
            int responseCode = ((HTTPException) throwable).getStatusCode();
            return responseCode == 401;
        }

        // CXF HTTPException without direct dependency (detected via reflection)
        if ("org.apache.cxf.transport.http.HTTPException".equals(throwable.getClass().getName())) {
            try {
                Object code = throwable.getClass().getMethod("getResponseCode").invoke(throwable);
                if (code instanceof Integer && ((Integer) code) == 401) {
                    return true;
                }
            } catch (Exception ignored) {
                // fall through to message check
            }
        }

        String message = throwable.getMessage();
        if (message != null && message.contains("401")) {
            return true;
        }

        return false;
    }
}
