package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.dto.AccountResponseDTO;
import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT MAX(a.accountNumber) FROM Account a WHERE a.accountNumber BETWEEN :minRange AND :maxRange")
    Long findMaxAccountNumberInRange(@Param("minRange") Long minRange, @Param("maxRange") Long maxRange);

    @Query("SELECT new edu.kennesaw.appdomain.dto.AccountResponseDTO(a.accountName, a.accountNumber," +
            " a.accountDescription, a.normalSide, a.accountCategory, a.accountSubCategory, a.initialBalance," +
            " a.debitBalance, a.creditBalance, a.dateAdded, u.username, a.isActive) FROM Account a JOIN a.creator" +
            " u ORDER BY a.accountNumber ASC")
    List<AccountResponseDTO> getChartOfAccountsWithUsername();

    Account findByAccountName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.creator = :newCreator WHERE a.creator = :oldCreator")
    void updateCreatorByCreator(@Param("oldCreator") User oldCreator, @Param("newCreator") User newCreator);

}
