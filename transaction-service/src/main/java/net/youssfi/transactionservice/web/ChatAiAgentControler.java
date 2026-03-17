package net.youssfi.transactionservice.web;

import net.youssfi.transactionservice.agent.TransactionAiAgent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin("*")
public class ChatAiAgentControler {

    private final TransactionAiAgent transactionAiAgent;

    public ChatAiAgentControler(TransactionAiAgent transactionAiAgent) {
        this.transactionAiAgent = transactionAiAgent;
    }

    @GetMapping("/ask")
    public Flux<String> askAgent(@RequestParam String question){
        return transactionAiAgent.chat(question);
    }
}
