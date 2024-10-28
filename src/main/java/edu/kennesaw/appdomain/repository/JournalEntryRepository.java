package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    JournalEntry findByPr(Long pr);
    List<JournalEntry> findAllByIsApproved(Boolean isApproved);
}
