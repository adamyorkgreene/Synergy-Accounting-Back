package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSecurityRepository extends JpaRepository<User, Long> {
    User findUserByUserSecurityId(Long userSecurityId);
    int getFailedLoginAttemptsByUsername(String username);
    boolean getIsVerifiedByUsername(String username);
    boolean getIsActiveByUsername(String username);

}
