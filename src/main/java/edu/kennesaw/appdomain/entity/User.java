package edu.kennesaw.appdomain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kennesaw.appdomain.UserType;
import jakarta.persistence.*;

import java.util.Date;
import java.util.Random;
import java.time.LocalDateTime;

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
    private Date birthday;

    /*@Column(nullable = false)
    private int birthMonth;

    @Column(nullable = false)
    private int birthYear;*/

    @Column(nullable = false)
    private Date joinDate;

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


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PasswordHistory> passwordHistories;

    @Column(nullable = false)
    private LocalDateTime passwordLastUpdated;

    private final int PASSWORD_EXPIRY_DAYS = 90;

    @Column
    private Date tempLeaveStart;

    @Column
    private Date tempLeaveEnd;


    public User() {
        Random ran = new Random();
        verificationCode = ran.nextInt(999999) + "";
        userType = UserType.DEFAULT;
        setIsVerified(false);
        this.passwordLastUpdated = LocalDateTime.now();
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

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setTempLeaveStart(Date tempLeaveStart) {
        this.tempLeaveStart = tempLeaveStart;
    }

    public Date getTempLeaveStart() {
        return tempLeaveStart;
    }

    public void setTempLeaveEnd(Date tempLeaveEnd) {
        this.tempLeaveEnd = tempLeaveEnd;
    }

    public Date getTempLeaveEnd() {
        return tempLeaveEnd;
    }

    /*public void setBirthMonth(Date birthMonth) {
        this.birthMonth = birthMonth;
    }

    public int getBirthMonth() {
        return birthMonth;
    }

    public void setBirthYear(Date birthYear) {
        this.birthYear = birthYear;
    }

    public int getBirthYear() {
        return birthYear;
    }*/

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

    public LocalDateTime getPasswordLastUpdated() {
        return passwordLastUpdated;
    }

    public void setPasswordLastUpdated(LocalDateTime passwordLastUpdated) {
        this.passwordLastUpdated = passwordLastUpdated;
    }

    public int getPasswordExpiryDays() {
        return PASSWORD_EXPIRY_DAYS;
    }

    @JsonProperty("isVerified")
    public boolean isVerified() {
        return isVerified;
    }

    @JsonProperty("failedLoginAttempts")
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public List<PasswordHistory> getPasswordHistories() {
        return passwordHistories;
    }

    @JsonProperty("failedLoginAttempts")
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }


    public void setPassword(String password) {
        this.password = password;
        this.passwordLastUpdated = LocalDateTime.now();

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;

    }

}

