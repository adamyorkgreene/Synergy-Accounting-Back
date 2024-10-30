package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.ConfirmationToken;
import edu.kennesaw.appdomain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationRepository extends JpaRepository<ConfirmationToken, Long> {
    ConfirmationToken findByToken(String token);
    void deleteByUser(User user);
}
