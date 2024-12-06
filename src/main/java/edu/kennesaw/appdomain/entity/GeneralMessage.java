package edu.kennesaw.appdomain.entity;

import jakarta.persistence.*;

@Entity
public class GeneralMessage {

    @Column(nullable = false)
    public String username;

    @Column(nullable = false)
    public String date;

    @Column(nullable = false)
    public String message;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
