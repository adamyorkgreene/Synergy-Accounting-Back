package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.UserType;

import java.util.Optional;

@SuppressWarnings("unused")
public class UserDTO {

    private Optional<String> username;

    private Optional<String> firstName;

    private Optional<String> lastName;

    private Optional<String> email;

    private Optional<Integer> birthday;

    private Optional<Integer> birthMonth;

    private Optional<Integer> birthYear;

    private Optional<Boolean> isVerified;

    private Optional<Boolean> isActive;

    private Optional<UserType> role;

    private Optional<String> address;

    private boolean isIncomplete;

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

    public void setBirthday(Optional<Integer> birthday) {
        this.birthday = birthday;
    }

    public void setBirthMonth(Optional<Integer> birthMonth) {
        this.birthMonth = birthMonth;
    }

    public void setBirthYear(Optional<Integer> birthYear) {
        this.birthYear = birthYear;
    }

    public void setRole(Optional<UserType> role) {
        this.role = role;
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

    public void setIsIncomplete(boolean isIncomplete) {
        this.isIncomplete = isIncomplete;
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

    public Optional<Integer> getBirthday() {
        return birthday;
    }

    public Optional<Integer> getBirthMonth() {
        return birthMonth;
    }

    public Optional<Integer> getBirthYear() {
        return birthYear;
    }

    public Optional<UserType> getRole() {
        return role;
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

    public boolean getIsIncomplete() {
        return isIncomplete;
    }
}
