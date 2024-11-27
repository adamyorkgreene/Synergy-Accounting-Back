package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAccountNumber(Long accountNumber);
    List<Transaction> findAllByAccountAndTransactionDateBetween(Account account, Date date1, Date date2);
    List<Transaction> findAllByAccountAndTransactionDateBefore(Account account, Date date);
    List<Transaction> findAllByAccount(Account account);
}
