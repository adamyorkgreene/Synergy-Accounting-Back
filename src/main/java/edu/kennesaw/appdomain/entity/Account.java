package edu.kennesaw.appdomain.entity;

import edu.kennesaw.appdomain.types.AccountCategory;
import edu.kennesaw.appdomain.types.AccountSubCategory;
import edu.kennesaw.appdomain.types.AccountType;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Account {

    @Column(nullable = false)
    private String accountName;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountNumber;

    @Column(nullable = false)
    private String accountDescription;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType normalSide;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountCategory accountCategory;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountSubCategory accountSubCategory;

    @Column(nullable = false)
    private double initialBalance;

    @Column(nullable = false)
    private double debitBalance;

    @Column(nullable = false)
    private double creditBalance;

    @Column(nullable = false)
    private double currentBalance;

    @Column(nullable = false)
    private Date dateAdded;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "userid")
    private User creator;

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountDescription(String accountDescription) {
        this.accountDescription = accountDescription;
    }

    public String getAccountDescription() {
        return accountDescription;
    }

    public void setNormalSide(AccountType normalSide) {
        this.normalSide = normalSide;
    }

    public AccountType getNormalSide() {
        return normalSide;
    }

    public void setAccountCategory(AccountCategory accountCategory) {
        this.accountCategory = accountCategory;
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public void setAccountSubCategory(AccountSubCategory accountSubCategory) {
        this.accountSubCategory = accountSubCategory;
    }

    public AccountSubCategory getAccountSubCategory() {
        return accountSubCategory;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setDebitBalance(double debitBalance) {
        this.debitBalance = debitBalance;
    }

    public double getDebitBalance() {
        return debitBalance;
    }

    public void setCreditBalance(double creditBalance) {
        this.creditBalance = creditBalance;
    }

    public double getCreditBalance() {
        return creditBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getCreator() {
        return creator;
    }

}
