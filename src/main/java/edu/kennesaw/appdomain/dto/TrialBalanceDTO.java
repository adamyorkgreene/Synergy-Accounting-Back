package edu.kennesaw.appdomain.dto;

public class TrialBalanceDTO {
    private String accountName;
    private Double debit;
    private Double credit;

    public TrialBalanceDTO(String accountName, Double debit, Double credit) {
        this.accountName = accountName;
        this.debit = debit;
        this.credit = credit;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Double getDebit() {
        return debit;
    }

    public void setDebit(Double debit) {
        this.debit = debit;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }
}
