package edu.kennesaw.appdomain.dto;

public class UpdateAccountDTO {

    private String accountName;
    private String accountDescription;
    private Long accountNumber;

    public UpdateAccountDTO(String accountName, String accountDescription, Long accountNumber) {
        this.accountName = accountName;
        this.accountDescription = accountDescription;
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountDescription() {
        return accountDescription;
    }

    public void setAccountDescription(String accountDescription) {
        this.accountDescription = accountDescription;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

}
