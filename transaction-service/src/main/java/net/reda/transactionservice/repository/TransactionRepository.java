package net.reda.transactionservice.repository;

import net.reda.transactionservice.entities.Transaction;
import net.reda.transactionservice.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(long accountId);
    List<Transaction> findByStatus(TransactionStatus transactionStatus);
}
