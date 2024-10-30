package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user);
    void deleteByUser(User user);
}
