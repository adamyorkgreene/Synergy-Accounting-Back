package edu.kennesaw.appdomain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "old_passwords")
public class OldPassword {

    @Id
    @JsonProperty("old_password_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_security_id", nullable = false)
    @JsonBackReference
    private UserSecurity userSecurity;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Date passwordChangedAt;

    public OldPassword() {}

    public OldPassword(UserSecurity userSecurity, String password, Date passwordChangedAt) {
        this.userSecurity = userSecurity;
        this.password = password;
        this.passwordChangedAt = passwordChangedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserSecurity(UserSecurity userSecurity) {
        this.userSecurity = userSecurity;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordChangedAt(Date passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public Long getId() {
        return id;
    }

    public UserSecurity getUserSecurity(UserSecurity userSecurity) {
        return this.userSecurity;
    }

    public String getPassword() {
        return password;
    }

    public Date getPasswordChangedAt() {
        return passwordChangedAt;
    }

}
