package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.types.AccountCategory;
import edu.kennesaw.appdomain.types.AccountSubCategory;
import edu.kennesaw.appdomain.types.AccountType;
import java.util.Date;

public class AccountDTO {

    private String accountName;
    private Long accountNumber;
    private String accountDescription;
    private AccountType normalSide;
    private AccountCategory accountCategory;
    private AccountSubCategory accountSubCategory;
    private Double initialBalance = 0.0;
    private Integer creator;
    private Boolean isActive;

    // Getters and Setters
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

    public double getInitialBalance() {
        if (initialBalance == null || initialBalance == 0 || initialBalance.isNaN()) {
            return 0.0;
        }
        return initialBalance;
    }

    public void setInitialBalance(Double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

}
