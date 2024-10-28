package edu.kennesaw.appdomain.repository;

import edu.kennesaw.appdomain.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, Long> {
    List<TransactionRequest> findByAccountAccountNumber(Long accountNumber);
    List<TransactionRequest> findAllByPr(Long pr);
    List<TransactionRequest> findAllByIsApprovedOrderByPr(Boolean approved);
    List<TransactionRequest> findAllByIsApprovedAndPr(Boolean approved, Long pr);
    TransactionRequest findByTransactionId(long id);
    void deleteByTransactionId(long id);
}
