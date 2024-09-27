package edu.kennesaw.appdomain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kennesaw.appdomain.UserType;
import jakarta.persistence.*;

import java.util.Random;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("userid")
    private Long userid;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private int birthday;

    @Column(nullable = false)
    private int birthMonth;

    @Column(nullable = false)
    private int birthYear;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String verificationCode;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isVerified;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isActive;

    @Column(nullable = false)
    private int failedLoginAttempts;

    public User() {
        Random ran = new Random();
        verificationCode = ran.nextInt(999999) + "";
        userType = UserType.DEFAULT;
        setIsVerified(false);
    }

    public void setUserid(Long id) {
        this.userid = id;
    }

    @JsonProperty("userid")
    public Long getUserid() { return userid; }

    public void setUserType(UserType userType) { this.userType = userType; }

    public UserType getUserType() {
        return userType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setBirthday(int birthday) {
        this.birthday = birthday;
    }

    public int getBirthday() {
        return birthday;
    }

    public void setBirthMonth(int birthMonth) {
        this.birthMonth = birthMonth;
    }

    public int getBirthMonth() {
        return birthMonth;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getVerificationCode() { return verificationCode; }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    @JsonProperty("isVerified")
    public boolean isVerified() {
        return isVerified;
    }

    @JsonProperty("failedLoginAttempts")
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    @JsonProperty("failedLoginAttempts")
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}

