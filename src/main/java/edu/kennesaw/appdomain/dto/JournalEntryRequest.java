package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.User;

public class JournalEntryRequest {

    private TransactionDTO[] transactions;
    private User user;
    private String comments;
    private Boolean isApproved;

    public void setTransactions(TransactionDTO[] transactions) {
        this.transactions = transactions;
    }

    public TransactionDTO[] getTransactions() {
        return transactions;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

}
