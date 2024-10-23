package edu.kennesaw.appdomain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @JsonProperty("user_security_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "userid")
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String emailPassword = generateRandomPassword();

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isVerified = false;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isActive = true;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isPasswordExpired = false;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @OneToMany(mappedBy = "userSecurity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<OldPassword> oldPasswords = new HashSet<>();

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean setPassword(String password) {
        if (this.password != null) {
            if (this.password.equals(password) || getOldPasswordsToStringList().contains(password)) {
                return false;
            } else {
                addOldPassword(this.password);
                this.password = password;
                return true;
            }
        } else {
            this.password = password;
            return true;
        }
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    @JsonProperty("is_active")
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public void addOldPassword(String oldPassword) {
        this.oldPasswords.add(new OldPassword(this, oldPassword, new Date()));
    }

    public void setOldPasswords(Set<OldPassword> oldPasswords) {
        this.oldPasswords = oldPasswords;
    }

    public void setIsPasswordExpired(boolean isPasswordExpired) {
        this.isPasswordExpired = isPasswordExpired;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Set<OldPassword> getOldPasswords() {
        return oldPasswords;
    }

    public List<String> getOldPasswordsToStringList() {
        List<String> oldPasswordsStringList = new ArrayList<>();
        for (OldPassword op : oldPasswords) {
            oldPasswordsStringList.add(op.getPassword());
        }
        return oldPasswordsStringList;
    }

    public boolean getIsPasswordExpired() {
        return isPasswordExpired;
    }

    private String generateRandomPassword() {
        int length = 16;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        return password.toString();
    }
}
