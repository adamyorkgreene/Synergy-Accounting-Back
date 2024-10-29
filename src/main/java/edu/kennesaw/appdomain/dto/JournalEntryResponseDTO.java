package edu.kennesaw.appdomain.dto;

public class JournalEntryResponseDTO {
    private MessageResponse messageResponse;
    private Long id;
    public MessageResponse getMessageResponse() {
        return messageResponse;
    }
    public void setMessageResponse(MessageResponse messageResponse) {
        this.messageResponse = messageResponse;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
