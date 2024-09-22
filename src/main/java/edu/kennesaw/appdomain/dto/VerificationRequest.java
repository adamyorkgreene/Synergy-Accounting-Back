package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.User;

public class VerificationRequest {

    private User user;
    private int verificationCode;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(int verificationCode) {
        this.verificationCode = verificationCode;
    }

}
