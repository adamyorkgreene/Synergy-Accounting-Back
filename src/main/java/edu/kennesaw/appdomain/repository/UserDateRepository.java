package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserDateRepository extends JpaRepository<UserDate, Long> {
    UserDate findByUser(User user);
    UserDate findById(long id);
    void deleteByUser(User user);
    List<UserDate> findUserDatesByLastPasswordResetBefore(Date date);
    List<UserDate> findUserDatesByLastPasswordResetAfter(Date date);
    List<UserDate> findUserDatesByLastPasswordResetBetween(Date date1, Date date2);
}
