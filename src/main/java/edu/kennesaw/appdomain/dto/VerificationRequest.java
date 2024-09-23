package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.User;

public class VerificationRequest {

    private Long userid;
    private String verificationCode;

    public Long getUserId() {
        return userid;
    }

    public void setUserId(Long id) {
        this.userid = id;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

}
