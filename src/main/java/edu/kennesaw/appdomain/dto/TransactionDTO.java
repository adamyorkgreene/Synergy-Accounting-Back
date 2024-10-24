package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.types.AccountType;

import java.util.Date;

public class TransactionDTO {

    private Account account;
    private String transactionDescription;
    private Double transactionAmount;
    private AccountType transactionType;
    private Long transactionId;
    private Date transactionDate;

    public TransactionDTO(Account account, String transactionDescription, Double transactionAmount, AccountType
            transactionType) {
        this.account = account;
        this.transactionDescription = transactionDescription;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public AccountType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(AccountType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
}
