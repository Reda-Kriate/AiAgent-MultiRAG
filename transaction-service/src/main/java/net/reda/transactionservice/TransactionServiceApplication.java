package net.reda.transactionservice;

import net.reda.transactionservice.entities.Transaction;
import net.reda.transactionservice.entities.TransactionStatus;
import net.reda.transactionservice.entities.TransactionType;
import net.reda.transactionservice.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;

@SpringBootApplication
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(TransactionRepository transactionRepository){
        return args -> {
            List<Long> accounts = List.of(Long.valueOf(11),Long.valueOf(22),Long.valueOf(33));
            accounts.forEach(accountId->{
                for (TransactionType type : TransactionType.values()){
                    for (int i = 0; i <3 ; i++) {
                        Transaction transaction = Transaction.builder()
                                .accountId(accountId)
                                .type(type)
                                .amount(1000+ Math.random()*70000)
                                .date(new Date())
                                .status(TransactionStatus.PENDING)
                                .build();
                       transactionRepository.save(transaction);
                    }
                }
            });
        };
    }
}
