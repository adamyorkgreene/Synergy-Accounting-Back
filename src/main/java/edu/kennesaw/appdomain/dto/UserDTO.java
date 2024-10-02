package edu.kennesaw.appdomain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kennesaw.appdomain.UserType;

import java.util.Date;
import java.util.Optional;

@SuppressWarnings("unused")
public class UserDTO {

    private Optional<Integer> userid = Optional.empty();

    private Optional<String> username;

    private Optional<String> firstName;

    private Optional<String> lastName;

    private Optional<String> email;

    private Optional<Date> birthday = Optional.empty();

    private Optional<Boolean> isVerified;

    private Optional<Boolean> isActive;

    private Optional<UserType> userType;

    private Optional<String> address;

    private Optional<Date> tempLeaveStart = Optional.empty();

    private Optional<Date> tempLeaveEnd = Optional.empty();

    private Optional<Integer> failedPasswordAttempts = Optional.empty();

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

    public boolean getIsIncomplete() {
        return isIncomplete;
    }
}
