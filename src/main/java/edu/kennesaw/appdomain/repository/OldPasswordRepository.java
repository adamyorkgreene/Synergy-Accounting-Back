package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.OldPassword;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserSecurity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface OldPasswordRepository extends JpaRepository<OldPassword, String> {

    List<OldPassword> findByUserSecurityOrderByPasswordChangedAtDesc(UserSecurity userSecurity);

    @Modifying
    @Transactional
    @Query("DELETE FROM OldPassword o WHERE o.userSecurity = :userSecurity")
    void deleteAllByUserSecurity(@Param("userSecurity") UserSecurity userSecurity);

}
