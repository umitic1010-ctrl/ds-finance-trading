package net.froihofer.dsfinance.bank.ejb.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Customer
 */
@Entity
@Table(name = "customer", indexes = {
    @Index(name = "idx_customer_number", columnList = "customer_number", unique = true)
})
@NamedQueries({
    @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c"),
    @NamedQuery(name = "Customer.findByCustomerNumber", query = "SELECT c FROM Customer c WHERE c.customerNumber = :customerNumber"),
    @NamedQuery(name = "Customer.findByName", query = "SELECT c FROM Customer c WHERE LOWER(c.person.firstName) LIKE LOWER(:name) OR LOWER(c.person.lastName) LIKE LOWER(:name)"),
    @NamedQuery(name = "Customer.findByPersonId", query = "SELECT c FROM Customer c WHERE c.person.id = :personId")
})
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_number", unique = true, nullable = false, length = 50)
    private String customerNumber;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "country", length = 120)
    private String country;

    @Column(name = "postal_code", length = 40)
    private String postalCode;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "status", length = 30)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepotPosition> depotPositions = new ArrayList<>();

    public Customer() {
    }

    public Customer(String customerNumber, String address) {
        this.customerNumber = customerNumber;
        this.address = address;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DepotPosition> getDepotPositions() {
        return depotPositions;
    }

    public void setDepotPositions(List<DepotPosition> depotPositions) {
        this.depotPositions = depotPositions;
    }
}
