package edu.kennesaw.appdomain.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pr;

    @Column
    private String comments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "userid")
    private User user;

    @Column
    private Boolean isApproved = null;

    @Column
    private Date date;

    public Long getPr() {
        return pr;
    }

    public void setPr(Long pr) {
        this.pr = pr;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean isApproved() {
        return isApproved;
    }

    public void setApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
