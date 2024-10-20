package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.types.AccountCategory;
import edu.kennesaw.appdomain.types.AccountSubCategory;
import edu.kennesaw.appdomain.types.AccountType;

import java.util.Date;

public class AccountResponseDTO {

    private String accountName;
    private Long accountNumber;
    private String accountDescription;
    private AccountType normalSide;
    private AccountCategory accountCategory;
    private AccountSubCategory accountSubCategory;
    private Double initialBalance;
    private Double debitBalance;
    private Double creditBalance;
    private Date dateAdded;
    private String username;
    private Boolean isActive;

    public AccountResponseDTO(String accountName, Long accountNumber, String accountDescription, AccountType normalSide,
                              AccountCategory accountCategory, AccountSubCategory accountSubCategory, Double initialBalance,
                              Double debitBalance, Double creditBalance, Date dateAdded, String username, Boolean isActive) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.accountDescription = accountDescription;
        this.normalSide = normalSide;
        this.accountCategory = accountCategory;
        this.accountSubCategory = accountSubCategory;
        this.initialBalance = initialBalance;
        this.debitBalance = debitBalance;
        this.creditBalance = creditBalance;
        this.dateAdded = dateAdded;
        this.username = username;
        this.isActive = isActive;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountDescription() {
        return accountDescription;
    }

    public void setAccountDescription(String accountDescription) {
        this.accountDescription = accountDescription;
    }

    public AccountType getNormalSide() {
        return normalSide;
    }

    public void setNormalSide(AccountType normalSide) {
        this.normalSide = normalSide;
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public void setAccountCategory(AccountCategory accountCategory) {
        this.accountCategory = accountCategory;
    }

    public AccountSubCategory getAccountSubCategory() {
        return accountSubCategory;
    }

    public void setAccountSubCategory(AccountSubCategory accountSubCategory) {
        this.accountSubCategory = accountSubCategory;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(Double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Double getDebitBalance() {
        return debitBalance;
    }

    public void setDebitBalance(Double debitBalance) {
        this.debitBalance = debitBalance;
    }

    public double getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(Double creditBalance) {
        this.creditBalance = creditBalance;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
