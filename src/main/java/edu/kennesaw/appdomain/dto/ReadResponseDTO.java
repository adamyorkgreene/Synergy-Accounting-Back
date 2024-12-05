package edu.kennesaw.appdomain.dto;

public class ReadResponseDTO {

    private String id;
    private String username;

    public ReadResponseDTO(String id, String username) { this.id = id; this.username = username; }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
