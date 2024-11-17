package edu.kennesaw.appdomain.dto;

public class EmailAttachment {
    private String fileName;
    private String contentType;
    private String contentBase64;

    public EmailAttachment() {}

    public EmailAttachment(String fileName, String contentType, String contentBase64) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.contentBase64 = contentBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentBase64() {
        return contentBase64;
    }

    public void setContentBase64(String contentBase64) {
        this.contentBase64 = contentBase64;
    }

}
