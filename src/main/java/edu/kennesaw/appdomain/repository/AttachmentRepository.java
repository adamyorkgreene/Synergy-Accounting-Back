package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.Attachment;
import edu.kennesaw.appdomain.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByJe(JournalEntry je);
}
