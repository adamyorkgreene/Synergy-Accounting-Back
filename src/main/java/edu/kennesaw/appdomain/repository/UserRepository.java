package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.PasswordHistory;
import edu.kennesaw.appdomain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUserid(long userid);

// Retrieve all passwords used by the user
    @Query("SELECT ph.password FROM PasswordHistory ph WHERE ph.user.id = :userId")
    List<String> findAllPasswordsByUserId(@Param("userId") Long userId);

// Save a new password to history
    @Modifying
    @Query("INSERT INTO PasswordHistory (user, password, createdAt) VALUES (:user, :password, CURRENT_TIMESTAMP)")
    void saveNewPassword(@Param("user") User user, @Param("password") String password);

// Retrieve users whose password expires in the next 3 days
    @Query("SELECT u FROM User u WHERE u.passwordExpiryDate BETWEEN :currentDate AND :expiryDate")
    List<User> findUsersWithExpiringPasswords(@Param("currentDate") LocalDateTime currentDate, 
                                               @Param("expiryDate") LocalDateTime expiryDate);

}