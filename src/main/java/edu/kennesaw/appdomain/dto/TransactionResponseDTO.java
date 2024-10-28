package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.types.AccountType;

public class TransactionResponseDTO {

    private Long transactionId;
    private String transactionDate;
    private String transactionDescription;
    private Double transactionAmount;
    private AccountType transactionType;
    private Account account;
    private Long pr;

    public TransactionResponseDTO(Long transactionId, String transactionDate, String transactionDescription, Double transactionAmount, AccountType transactionType, Long pr) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.transactionDescription = transactionDescription;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
        this.pr = pr;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionType(AccountType transactionType) {
        this.transactionType = transactionType;
    }

    public AccountType getTransactionType() {
        return transactionType;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public Long getPr() {
        return pr;
    }

    public void setPr(Long pr) {
        this.pr = pr;
    }

}
