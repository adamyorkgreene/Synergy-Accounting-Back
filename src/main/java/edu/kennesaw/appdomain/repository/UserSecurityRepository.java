package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, Long> {
    UserSecurity getUserSecurityByUser(User user);
    List<UserSecurity> getAllByIsPasswordExpired(boolean isPasswordExpired);
}
