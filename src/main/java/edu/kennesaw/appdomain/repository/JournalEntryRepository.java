package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.JournalEntry;
import edu.kennesaw.appdomain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    JournalEntry findByPr(Long pr);

    List<JournalEntry> findAllByIsApproved(Boolean isApproved);

    List<JournalEntry> findAllByIsApprovedAndDateBetween(boolean isApproved, Date date1, Date date2);

    @Modifying
    @Transactional
    @Query("UPDATE JournalEntry a SET a.user = :newCreator WHERE a.user = :oldCreator")
    void updateCreatorByCreator(@Param("oldCreator") User oldCreator, @Param("newCreator") User newCreator);

}
