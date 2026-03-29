package net.reda.transactionservice.agent;

import dev.langchain4j.agent.tool.Tool;
import net.reda.transactionservice.entities.Transaction;
import net.reda.transactionservice.entities.TransactionStatus;
import net.reda.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionAiTools {

    private final TransactionRepository transactionRepository;

    public TransactionAiTools(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }


    @Tool("Retrieve all transactions from the database, regardless of account")
    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }

    @Tool("Retrieve all transactions associated with a specific account ID")
    public List<Transaction> findTransactionsById(long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Tool("Update the status of a specific transaction identified by its ID to a new TransactionStatus value (e.g. PENDING, COMPLETED, FAILED)")
    public Transaction updateTransactionStatus(long transactionId, TransactionStatus transactionStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setStatus(transactionStatus);
        transactionRepository.save(transaction);
        return transaction;
    }
}