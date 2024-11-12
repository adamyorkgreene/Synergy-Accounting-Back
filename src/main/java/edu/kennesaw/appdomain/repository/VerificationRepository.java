package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserSecurity;
import edu.kennesaw.appdomain.entity.VerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken v WHERE v.user = :user")
    void deleteAllByUser(@Param("user") User user);

}
