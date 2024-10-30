package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.OldPassword;
import edu.kennesaw.appdomain.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface OldPasswordRepository extends JpaRepository<OldPassword, String> {
    List<OldPassword> findByUserSecurityOrderByPasswordChangedAtDesc(UserSecurity userSecurity);
    void deleteAllByUserSecurity(UserSecurity userSecurity);
}
