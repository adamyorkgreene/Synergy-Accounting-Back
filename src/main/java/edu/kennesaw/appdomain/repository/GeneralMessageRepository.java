package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.GeneralMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneralMessageRepository extends JpaRepository<GeneralMessage, Long> {
    List<GeneralMessage> getAllByMessageIsNotNull();
}
