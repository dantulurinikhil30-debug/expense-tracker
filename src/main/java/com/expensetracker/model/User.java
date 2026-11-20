package com.expensetracker.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity representing an authenticated user with separate
 * Bank and Cash balances.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "bank_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal bankBalance = BigDecimal.ZERO;

    @Column(name = "cash_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;

    /**
     * User role for access control. Defaults to USER for normal registrations.
     * ADMIN role grants access to the Admin Panel.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentMode> paymentModes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    public User() {
    }

    public User(String fullName, String email, String password, BigDecimal bankBalance, BigDecimal cashBalance) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.bankBalance = bankBalance != null ? bankBalance : BigDecimal.ZERO;
        this.cashBalance = cashBalance != null ? cashBalance : BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    // --- Role Getters & Setters ---

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role != null ? role : Role.USER;
    }

    // --- Helper Business Methods ---

    /**
     * Calculates the total combined balance across Bank and Cash.
     */
    public BigDecimal getTotalBalance() {
        BigDecimal b = bankBalance != null ? bankBalance : BigDecimal.ZERO;
        BigDecimal c = cashBalance != null ? cashBalance : BigDecimal.ZERO;
        return b.add(c);
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBankBalance() {
        return bankBalance != null ? bankBalance : BigDecimal.ZERO;
    }

    public void setBankBalance(BigDecimal bankBalance) {
        this.bankBalance = bankBalance != null ? bankBalance : BigDecimal.ZERO;
    }

    public BigDecimal getCashBalance() {
        return cashBalance != null ? cashBalance : BigDecimal.ZERO;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance != null ? cashBalance : BigDecimal.ZERO;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<PaymentMode> getPaymentModes() {
        return paymentModes;
    }

    public void setPaymentModes(List<PaymentMode> paymentModes) {
        this.paymentModes = paymentModes;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", bankBalance=" + bankBalance +
                ", cashBalance=" + cashBalance +
                '}';
    }
}
