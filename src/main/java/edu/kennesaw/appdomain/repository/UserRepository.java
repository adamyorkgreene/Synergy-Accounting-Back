package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.dto.AccountResponseDTO;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserDate;
import edu.kennesaw.appdomain.types.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserid(long userid);

    List<User> findByUserDateIn(List<UserDate> userDates);

    @Query("SELECT u.username FROM User u WHERE u.userid = :userid")
    String getUsernameByUserid(long userid);

    Long findUserSecurityIdByEmail(String email);

    List<User> getAllUsersByUserType(UserType userType);

}
