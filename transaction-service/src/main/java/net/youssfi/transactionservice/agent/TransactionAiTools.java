package net.youssfi.transactionservice.agent;


import dev.langchain4j.agent.tool.Tool;
import net.youssfi.transactionservice.entities.Transaction;
import net.youssfi.transactionservice.entities.TransactionStatus;
import net.youssfi.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionAiTools {

    private final TransactionRepository transactionRepository;

    public TransactionAiTools(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool("get all transactions")
    public List<Transaction> findAllTransactions(){
        return transactionRepository.findAll();
    }

    @Tool("get  transactions by account ID ")
    public List<Transaction> findTransactionsById(long id){
        return transactionRepository.findByAccountId(id);
    }

    @Tool("get  transactions by account ID ")
    public Transaction updateTransactionStatus(long id, TransactionStatus transactionStatus){
        Transaction byStatus = transactionRepository.findById(id).get();
        byStatus.setStatus(transactionStatus);
        transactionRepository.save(byStatus);
        return byStatus;
    }

}
