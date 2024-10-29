package edu.kennesaw.appdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "pr")
    private JournalEntry je;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    // Constructors
    public Attachment() {
        this.uploadedAt = LocalDateTime.now(); // Set upload timestamp
    }

    public Attachment(String fileName, String filePath, JournalEntry je) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.je = je;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public JournalEntry getJournalEntry() {
        return je;
    }

    public void setJournalEntry(JournalEntry je) {
        this.je = je;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
