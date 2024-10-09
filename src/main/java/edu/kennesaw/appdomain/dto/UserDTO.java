package edu.kennesaw.appdomain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kennesaw.appdomain.types.UserType;

import java.util.Date;
import java.util.Optional;

@SuppressWarnings("unused")
public class UserDTO {

    private Optional<Integer> userid = Optional.empty();

    private Optional<String> username = Optional.empty();

    private Optional<String> firstName = Optional.empty();

    private Optional<String> lastName = Optional.empty();

    private Optional<String> email = Optional.empty();

    private Optional<Date> birthday = Optional.empty();

    private Optional<Boolean> isVerified = Optional.empty();

    private Optional<Boolean> isActive = Optional.empty();

    private Optional<UserType> userType = Optional.empty();

    private Optional<String> address = Optional.empty();

    private Optional<Date> tempLeaveStart = Optional.empty();

    private Optional<Date> tempLeaveEnd = Optional.empty();

    private Optional<Integer> failedPasswordAttempts = Optional.empty();

    private Optional<String> emailPassword = Optional.empty();

    private boolean isIncomplete;

    public void setUserid(Optional<Integer> userid) {
        this.userid = userid;
    }

    public void setUsername(Optional<String> username) {
        this.username = username;
    }

    public void setFirstName(Optional<String> firstName) {
        this.firstName = firstName;
    }

    public void setLastName(Optional<String> lastName) {
        this.lastName = lastName;
    }

    public void setEmail(Optional<String> email) {
        this.email = email;
    }

    @JsonProperty("birthday")
    public void setBirthday(Optional<Date> birthday) {
        this.birthday = birthday;
    }

    public void setTempLeaveStart(Optional<Date> tempLeaveStart) {
        this.tempLeaveStart = tempLeaveStart;
    }

    public void setTempLeaveEnd(Optional<Date> tempLeaveEnd) {
        this.tempLeaveEnd = tempLeaveEnd;
    }

    public void setUserType(Optional<UserType> userType) {
        this.userType = userType;
    }

    public void setIsVerified(Optional<Boolean> isVerified) {
        this.isVerified = isVerified;
    }

    public void setIsActive(Optional<Boolean> isActive) {
        this.isActive = isActive;
    }

    public void setAddress(Optional<String> address) {
        this.address = address;
    }

    public void setFailedPasswordAttempts(Optional<Integer> failedPasswordAttempts) {
        this.failedPasswordAttempts = failedPasswordAttempts;
    }

    public void setIsIncomplete(boolean isIncomplete) {
        this.isIncomplete = isIncomplete;
    }

    public void setEmailPassword(Optional<String> emailPassword) {
        this.emailPassword = emailPassword;
    }

    public Optional<Integer> getUserid() {
        return userid;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getFirstName() {
        return firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }

    public Optional<String> getEmail() {
        return email;
    }

    @JsonProperty("birthday")
    public Optional<Date> getBirthday() {
        return birthday;
    }

    public Optional<Date> getTempLeaveStart() {
        return tempLeaveStart;
    }

    public Optional<Date> getTempLeaveEnd() {
        return tempLeaveEnd;
    }

    public Optional<UserType> getUserType() {
        return userType;
    }

    public Optional<Boolean> getIsVerified() {
        return isVerified;
    }

    public Optional<Boolean> getIsActive() {
        return isActive;
    }

    public Optional<String> getAddress() {
        return address;
    }

    public Optional<Integer> getFailedPasswordAttempts() {
        return failedPasswordAttempts;
    }

    public Optional<String> getEmailPassword() {
        return emailPassword;
    }

    public boolean getIsIncomplete() {
        return isIncomplete;
    }
}
