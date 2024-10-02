package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUserid(long userid);
    List<User> findAllByLastPasswordResetIsBetween(Date before, Date after);
}