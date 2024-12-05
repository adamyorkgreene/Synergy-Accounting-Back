package edu.kennesaw.appdomain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class AdminEmailObject {

    private String to;

    private String from;

    private Date date;

    private String subject;

    private String body;

    private String id;

    private boolean isRead;

    private List<EmailAttachment> attachments = null;

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setId(String id) { this.id = id; }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public void setIsRead(boolean isRead) { this.isRead = isRead; }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public Date getDate() { return date;}

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getId() { return id; }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    @JsonProperty("isRead")
    public boolean isRead() { return isRead; }

}

