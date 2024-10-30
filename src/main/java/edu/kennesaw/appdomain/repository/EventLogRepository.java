package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.EventLog;
import edu.kennesaw.appdomain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findByAccountId(Long accountId);

    @Modifying
    @Transactional
    @Query("UPDATE EventLog a SET a.userId = :newCreatorId WHERE a.userId = :oldCreatorId")
    void updateUserIdByUserId(@Param("oldCreatorId") Long oldCreatorId, @Param("newCreatorId") Long newCreatorId);

}
