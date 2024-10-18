package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAccountNumber(Long accountNumber);
}
