package net.froihofer.dsfinance.bank.ejb.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * JPA Entity for Depot Position (Stock holding for a customer)
 */
@Entity
@Table(name = "DEPOT_POSITION", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_id", "stock_symbol"})
})
@NamedQueries({
    @NamedQuery(name = "DepotPosition.findByCustomer",
                query = "SELECT d FROM DepotPosition d WHERE d.customer.id = :customerId"),
    @NamedQuery(name = "DepotPosition.findByCustomerAndStock",
                query = "SELECT d FROM DepotPosition d WHERE d.customer.id = :customerId AND d.stockSymbol = :stockSymbol")
})
public class DepotPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "stock_symbol", nullable = false, length = 20)
    private String stockSymbol;

    @Column(name = "stock_name", length = 255)
    private String stockName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    public DepotPosition() {
    }

    public DepotPosition(Customer customer, String stockSymbol, String stockName, Integer quantity) {
        this.customer = customer;
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void addQuantity(Integer amount) {
        this.quantity += amount;
    }

    public void subtractQuantity(Integer amount) {
        this.quantity -= amount;
    }
}

