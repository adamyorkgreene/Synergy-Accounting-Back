package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, Long> {
    List<TransactionRequest> findByAccountAccountNumber(Long accountNumber);
    List<TransactionRequest> findAllByToken(String token);
    List<TransactionRequest> findAllByIsApprovedOrderByToken(Boolean approved);
    TransactionRequest findByTransactionId(long id);
}
