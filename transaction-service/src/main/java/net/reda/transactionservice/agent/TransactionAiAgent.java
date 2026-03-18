package net.reda.transactionservice.agent;

import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface TransactionAiAgent {
    Flux<String> chat(String question);
}
