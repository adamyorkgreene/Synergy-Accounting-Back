package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.User;

public class AdminEmailObject {

    private String to;

    private User from;

    private String subject;

    private String body;

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public User getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

}
