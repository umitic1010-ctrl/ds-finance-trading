package net.froihofer.dsfinance.bank.client;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.froihofer.dsfinance.bank.common.dto.BankVolumeDTO;
import net.froihofer.dsfinance.bank.common.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.DepotPositionDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.bank.common.dto.TradeRequestDTO;
import net.froihofer.dsfinance.bank.common.exception.BankingException;
import net.froihofer.dsfinance.bank.common.remote.CustomerBankingRemote;
import net.froihofer.dsfinance.bank.common.remote.EmployeeBankingRemote;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple CLI client that demonstrates how to invoke the remote EJB interfaces.
 */
public class BankClient {
  private static final Logger log = LoggerFactory.getLogger(BankClient.class);
  private static final String APPLICATION_NAME = "ds-finance-bank-ear";
  private static final String MODULE_NAME = "ds-finance-bank-ejb";

  private final Scanner scanner = new Scanner(System.in);

  private EmployeeBankingRemote employeeRemote;
  private CustomerBankingRemote customerRemote;

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Usage: java -jar ds-finance-bank-client.jar <employee|customer> <username> <password>");
      return;
    }

    BankClient client = new BankClient();
    client.run(args[0], args[1], args[2]);
  }

  private void run(String role, String username, String password) {
    try {
      WildflyJndiLookupHelper helper = createLookupHelper(username, password);
      if ("employee".equalsIgnoreCase(role)) {
        employeeRemote = helper.lookupUsingJBossEjbClient("EmployeeBankingBean", EmployeeBankingRemote.class, true);
        log.info("Signed in as employee {}", username);
        employeeLoop();
      } else if ("customer".equalsIgnoreCase(role)) {
        customerRemote = helper.lookupUsingJBossEjbClient("CustomerBankingBean", CustomerBankingRemote.class, true);
        log.info("Signed in as customer {}", username);
        customerLoop();
      } else {
        System.out.println("Unknown role '" + role + "'. Expecting 'employee' or 'customer'.");
      }
    } catch (NamingException e) {
      log.error("Failed to obtain remote bean", e);
      System.out.println("Could not connect to server: " + e.getMessage());
    }
  }

  private WildflyJndiLookupHelper createLookupHelper(String username, String password) throws NamingException {
    Properties props = loadJndiProperties();
    AuthCallbackHandler.setUsername(username);
    AuthCallbackHandler.setPassword(password);
    props.put(Context.SECURITY_PRINCIPAL, username);
    props.put(Context.SECURITY_CREDENTIALS, password);
    InitialContext initialContext = new InitialContext(props);
    return new WildflyJndiLookupHelper(initialContext, APPLICATION_NAME, MODULE_NAME, "");
  }

  private Properties loadJndiProperties() {
    Properties props = new Properties();
    try (InputStream in = BankClient.class.getClassLoader().getResourceAsStream("jndi.properties")) {
      if (in != null) {
        props.load(in);
      }
    } catch (IOException e) {
      log.warn("Could not load jndi.properties from classpath", e);
    }
    return props;
  }

  private void employeeLoop() {
    while (true) {
      System.out.println();
      System.out.println("Employee Menu");
      System.out.println("1) List customers");
      System.out.println("2) Search customers");
      System.out.println("3) Create customer");
      System.out.println("4) View customer depot");
      System.out.println("5) Buy stocks for customer");
      System.out.println("6) Sell stocks for customer");
      System.out.println("7) View bank volume");
      System.out.println("8) Search stocks");
      System.out.println("9) Quit");
      System.out.print("Select option: ");

      String choice = scanner.nextLine();
      try {
        switch (choice) {
          case "1" -> listCustomers();
          case "2" -> searchCustomers();
          case "3" -> createCustomer();
          case "4" -> showDepotForCustomer();
          case "5" -> buyForCustomer();
          case "6" -> sellForCustomer();
          case "7" -> showBankVolume();
          case "8" -> searchStocks(employeeRemote);
          case "9" -> {
            System.out.println("Goodbye");
            return;
          }
          default -> System.out.println("Unknown option");
        }
      } catch (BankingException e) {
        System.out.println("Operation failed: " + e.getMessage());
      }
    }
  }

  private void customerLoop() {
    while (true) {
      System.out.println();
      System.out.println("Customer Menu");
      System.out.println("1) View my depot");
      System.out.println("2) Buy stocks");
      System.out.println("3) Sell stocks");
      System.out.println("4) Search stocks");
      System.out.println("5) Quit");
      System.out.print("Select option: ");

      String choice = scanner.nextLine();
      try {
        switch (choice) {
          case "1" -> showOwnDepot();
          case "2" -> buyAsCustomer();
          case "3" -> sellAsCustomer();
          case "4" -> searchStocks(customerRemote);
          case "5" -> {
            System.out.println("Goodbye");
            return;
          }
          default -> System.out.println("Unknown option");
        }
      } catch (BankingException e) {
        System.out.println("Operation failed: " + e.getMessage());
      }
    }
  }

  private void listCustomers() throws BankingException {
    List<CustomerDTO> customers = employeeRemote.listCustomers();
    if (customers.isEmpty()) {
      System.out.println("No customers available");
      return;
    }
    customers.forEach(this::printCustomer);
  }

  private void searchCustomers() throws BankingException {
    System.out.print("Search term (name or customer number): ");
    String query = scanner.nextLine();
    List<CustomerDTO> customers = employeeRemote.searchCustomers(query);
    customers.forEach(this::printCustomer);
  }

  private void createCustomer() throws BankingException {
    CustomerDTO dto = new CustomerDTO();
    System.out.print("Customer number: ");
    dto.setCustomerNumber(scanner.nextLine());
    System.out.print("First name: ");
    dto.setFirstName(scanner.nextLine());
    System.out.print("Last name: ");
    dto.setLastName(scanner.nextLine());
    System.out.print("Address: ");
    dto.setAddress(scanner.nextLine());
    System.out.print("Email (optional): ");
    dto.setEmail(scanner.nextLine());

    CustomerDTO created = employeeRemote.createCustomer(dto);
    System.out.println("Created customer: ");
    printCustomer(created);
  }

  private void showDepotForCustomer() throws BankingException {
    System.out.print("Customer number: ");
    String customerNumber = scanner.nextLine();
    DepotDTO depot = employeeRemote.getDepot(customerNumber);
    printDepot(depot);
  }

  private void buyForCustomer() throws BankingException {
    System.out.print("Customer number: ");
    String customerNumber = scanner.nextLine();
    TradeRequestDTO request = readTradeRequest();
    int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
    employeeRemote.buyStocks(customerNumber, request.getStockSymbol(), quantity);
    System.out.println("Buy order executed");
  }

  private void sellForCustomer() throws BankingException {
    System.out.print("Customer number: ");
    String customerNumber = scanner.nextLine();
    TradeRequestDTO request = readTradeRequest();
    int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
    employeeRemote.sellStocks(customerNumber, request.getStockSymbol(), quantity);
    System.out.println("Sell order executed");
  }

  private void showBankVolume() throws BankingException {
    BankVolumeDTO volume = employeeRemote.getBankVolume();
    System.out.println("Available volume: " + formatMoney(volume.getAvailableVolume(), volume.getCurrency()));
    System.out.println("Initial volume:   " + formatMoney(volume.getInitialVolume(), volume.getCurrency()));
  }

  private void showOwnDepot() throws BankingException {
    DepotDTO depot = customerRemote.getMyDepot();
    printDepot(depot);
  }

  private void buyAsCustomer() throws BankingException {
    TradeRequestDTO request = readTradeRequest();
    customerRemote.buyStocks(request);
    System.out.println("Buy order executed");
  }

  private void sellAsCustomer() throws BankingException {
    TradeRequestDTO request = readTradeRequest();
    customerRemote.sellStocks(request);
    System.out.println("Sell order executed");
  }

  private void searchStocks(EmployeeBankingRemote remote) throws BankingException {
    System.out.print("Search term (optional): ");
    String query = scanner.nextLine();
    List<StockDTO> stocks = remote.searchStocks(query);
    printStocks(stocks);
  }

  private void searchStocks(CustomerBankingRemote remote) throws BankingException {
    System.out.print("Search term (optional): ");
    String query = scanner.nextLine();
    List<StockDTO> stocks = remote.searchStocks(query);
    printStocks(stocks);
  }

  private TradeRequestDTO readTradeRequest() {
    TradeRequestDTO request = new TradeRequestDTO();
    System.out.print("Stock symbol: ");
    request.setStockSymbol(scanner.nextLine());
    System.out.print("Quantity: ");
    String qty = scanner.nextLine();
    try {
      request.setQuantity(Integer.parseInt(qty));
    } catch (NumberFormatException e) {
      System.out.println("Invalid quantity, defaulting to 0");
      request.setQuantity(0);
    }
    return request;
  }

  private void printCustomer(CustomerDTO customer) {
    System.out.println("- " + customer.getCustomerNumber() + " | " + customer.getFirstName() + " " + customer.getLastName());
    if (customer.getAddress() != null && !customer.getAddress().isBlank()) {
      System.out.println("  Address: " + customer.getAddress());
    }
    if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
      System.out.println("  Email:   " + customer.getEmail());
    }
  }

  private void printDepot(DepotDTO depot) {
    System.out.println("Depot for customer " + depot.getCustomerNumber());
    if (depot.getPositions().isEmpty()) {
      System.out.println("  No holdings");
    } else {
      for (DepotPositionDTO position : depot.getPositions()) {
        int quantity = position.getQuantity() != null ? position.getQuantity() : 0;
        BigDecimal price = position.getCurrentPrice() != null ? position.getCurrentPrice() : BigDecimal.ZERO;
        BigDecimal total = position.getTotalValue();
        if (total == null) {
          total = price.multiply(BigDecimal.valueOf(quantity));
        }
        System.out.println("  " + position.getStockSymbol() + " - " + position.getStockName());
        System.out.println("    Quantity: " + quantity);
        System.out.println("    Price:    " + price);
        System.out.println("    Total:    " + total);
      }
    }
    BigDecimal portfolioValue = depot.getTotalValue() != null ? depot.getTotalValue() : BigDecimal.ZERO;
    System.out.println("  Portfolio value: " + portfolioValue);
  }

  private void printStocks(List<StockDTO> stocks) {
    if (stocks.isEmpty()) {
      System.out.println("No stocks found");
      return;
    }
    stocks.forEach(stock -> System.out.println("- " + stock.getSymbol() + " | " + stock.getName() + " | Price: " + stock.getCurrentPrice()));
  }

  private String formatMoney(BigDecimal amount, String currency) {
    String code = Objects.requireNonNullElse(currency, "USD");
    return amount + " " + code;
  }
}
