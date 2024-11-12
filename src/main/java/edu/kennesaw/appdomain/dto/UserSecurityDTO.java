package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.UserSecurity;

public class UserSecurityDTO {

    private boolean isVerified;
    private boolean isActive;
    private int failedLoginAttempts;

    public UserSecurityDTO() {}

    public UserSecurityDTO(UserSecurity userSecurity) {
        this.isActive = userSecurity.getIsActive();
        this.isVerified = userSecurity.getIsVerified();
        this.failedLoginAttempts = userSecurity.getFailedLoginAttempts();
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
}
