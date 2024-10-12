package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.types.AccountType;

import java.util.Date;

public class TransactionDTO {

    private Long accountNumber;
    private String transactionDescription;
    private Double transactionAmount;
    private AccountType transactionType;
    private Long transactionId;

    public TransactionDTO(Long accountNumber, String transactionDescription, Double transactionAmount, AccountType
            transactionType) {
        this.accountNumber = accountNumber;
        this.transactionDescription = transactionDescription;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
    }

    public void setAccount(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getAccount() {
        return accountNumber;
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
}
