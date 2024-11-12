package edu.kennesaw.appdomain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "user_dates")
public class UserDate {

    @Id
    @JsonProperty("user_date_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "userid")
    @JsonBackReference
    private User user;

    @Column
    private Date birthday;

    @Column(nullable = false)
    private Date joinDate = new Date();

    @Column
    private Date tempLeaveStart;

    @Column
    private Date tempLeaveEnd;

    @Column(nullable = false)
    private Date lastPasswordReset = new Date();

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public void setTempLeaveStart(Date tempLeaveStart) {
        this.tempLeaveStart = tempLeaveStart;
    }

    public void setTempLeaveEnd(Date tempLeaveEnd) {
        this.tempLeaveEnd = tempLeaveEnd;
    }

    public void setLastPasswordReset(Date lastPasswordReset) {
        this.lastPasswordReset = lastPasswordReset;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public Date getTempLeaveStart() {
        return tempLeaveStart;
    }

    public Date getTempLeaveEnd() {
        return tempLeaveEnd;
    }

    public Date getLastPasswordReset() {
        return lastPasswordReset;
    }

}
