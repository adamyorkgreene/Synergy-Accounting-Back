package edu.kennesaw.appdomain.dto;

public class GenMessageDTO {

    public String username;

    public String date;

    public String message;

    public GenMessageDTO(String username, String date, String message) {
        this.username = username;
        this.date = date;
        this.message = message;
    }

    public void setMessage() {
        this.message = message;
    }

    public void setDate() {
        this.date = date;
    }

    public void setUsername() {
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

}
