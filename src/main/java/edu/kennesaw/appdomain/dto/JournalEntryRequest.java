package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.User;

public class JournalEntryRequest {

    private TransactionDTO[] transactions;
    private User user;

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

}
