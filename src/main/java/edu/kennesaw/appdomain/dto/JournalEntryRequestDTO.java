package edu.kennesaw.appdomain.dto;

public class JournalEntryRequestDTO {

    private Long[] ids;
    private String comments;

    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
