package net.froihofer.dsfinance.bank.ejb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "employee")
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long personId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "employee_number", length = 50)
    private String employeeNumber;

    @Column(name = "department", length = 100)
    private String department;

    public Employee() {
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
